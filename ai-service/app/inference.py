from __future__ import annotations

import math
import threading
import unicodedata
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Mapping, Protocol, Sequence

from .schemas import ModerationResponse, ModerationScores
from .settings import Settings


EXPECTED_LABELS = ("CLEAN", "OFFENSIVE", "HATE")
SEVERITY = {"CLEAN": 0, "OFFENSIVE": 1, "HATE": 2}


class ModelConfigurationError(RuntimeError):
    """Checkpoint không đáp ứng contract label hoặc kiến trúc đã khóa."""


class ModerationEngine(Protocol):
    def moderate(self, text: str) -> ModerationResponse:
        """Phân loại một nội dung đã được API validate."""


@dataclass(frozen=True)
class ChunkPrediction:
    label: str
    confidence: float
    scores: Mapping[str, float]


def validate_label_mapping(id2label: Mapping[Any, Any]) -> dict[int, str]:
    """Không suy đoán index: checkpoint phải công bố đúng ba label nghiệp vụ."""
    try:
        normalized = {int(index): str(label).strip().upper() for index, label in id2label.items()}
    except (TypeError, ValueError) as exception:
        raise ModelConfigurationError("id2label của checkpoint không hợp lệ") from exception
    expected = {0: "CLEAN", 1: "OFFENSIVE", 2: "HATE"}
    if normalized != expected:
        raise ModelConfigurationError(f"id2label không đúng contract bắt buộc: {normalized}")
    return normalized


def aggregate_predictions(predictions: Sequence[ChunkPrediction]) -> ChunkPrediction:
    """Đoạn nghiêm trọng nhất thắng; cùng mức thì chọn confidence cao nhất."""
    if not predictions:
        raise ValueError("Phải có ít nhất một kết quả chunk")
    for prediction in predictions:
        if prediction.label not in SEVERITY:
            raise ModelConfigurationError(f"Model trả label không hỗ trợ: {prediction.label}")
    return max(predictions, key=lambda item: (SEVERITY[item.label], item.confidence))


class PhoBertHsdEngine:
    """Runtime CPU-first, nạp model một lần và chia input theo token để không mất phần đuôi."""

    def __init__(
        self,
        tokenizer: Any,
        model: Any,
        torch_module: Any,
        id2label: Mapping[int, str],
        device: str,
        max_sequence_length: int,
    ) -> None:
        self._tokenizer = tokenizer
        self._model = model
        self._torch = torch_module
        self._id2label = validate_label_mapping(id2label)
        self._device = device
        self._max_sequence_length = max_sequence_length
        self._payload_length = max_sequence_length - tokenizer.num_special_tokens_to_add(pair=False)
        if self._payload_length < 1:
            raise ModelConfigurationError("Giới hạn context không đủ chứa payload")
        self._inference_lock = threading.Lock()

    @classmethod
    def load(cls, settings: Settings) -> "PhoBertHsdEngine":
        # Import trễ giúp unit test API không phải cài/tải PyTorch và model thật.
        import torch
        import torch.nn as nn
        from huggingface_hub import hf_hub_download
        from safetensors.torch import load_model
        from transformers import AutoConfig, AutoModel, AutoTokenizer

        cache_dir = Path(settings.model_cache_dir).resolve()
        cache_dir.mkdir(parents=True, exist_ok=True)

        checkpoint_config = AutoConfig.from_pretrained(
            settings.model_name,
            revision=settings.model_revision,
            cache_dir=cache_dir,
            trust_remote_code=False,
        )
        id2label = validate_label_mapping(checkpoint_config.id2label)
        base_config = AutoConfig.from_pretrained(
            settings.base_model_name,
            revision=settings.base_model_revision,
            cache_dir=cache_dir,
            trust_remote_code=False,
        )
        tokenizer = AutoTokenizer.from_pretrained(
            settings.model_name,
            revision=settings.model_revision,
            cache_dir=cache_dir,
            trust_remote_code=False,
            use_fast=False,
        )

        class CheckpointArchitecture(nn.Module):
            """Kiến trúc tối thiểu khớp chính xác models.PhoBERTV2Model của checkpoint."""

            def __init__(self) -> None:
                super().__init__()
                self.encoder = AutoModel.from_config(base_config)
                self.dropout = nn.Dropout(0.1)
                self.classifier = nn.Linear(base_config.hidden_size, len(id2label))

            def forward(self, input_ids: Any, attention_mask: Any) -> Any:
                outputs = self.encoder(input_ids=input_ids, attention_mask=attention_mask)
                pooled_output = outputs.pooler_output
                return self.classifier(self.dropout(pooled_output))

        model = CheckpointArchitecture()
        checkpoint_path = hf_hub_download(
            repo_id=settings.model_name,
            filename="model.safetensors",
            revision=settings.model_revision,
            cache_dir=cache_dir,
        )
        missing, unexpected = load_model(model, checkpoint_path, strict=False, device="cpu")
        if missing or unexpected:
            raise ModelConfigurationError(
                f"Checkpoint không khớp kiến trúc; missing={missing}, unexpected={unexpected}"
            )

        device = cls._resolve_device(settings.device, torch)
        model.to(device)
        model.eval()

        special_tokens = tokenizer.num_special_tokens_to_add(pair=False)
        architecture_limit = int(base_config.max_position_embeddings) - special_tokens
        max_sequence_length = min(settings.max_sequence_length, architecture_limit)
        if max_sequence_length <= special_tokens:
            raise ModelConfigurationError("AI_MODEL_MAX_LENGTH không hợp lệ")
        return cls(tokenizer, model, torch, id2label, device, max_sequence_length)

    @staticmethod
    def _resolve_device(configured: str, torch_module: Any) -> str:
        if configured == "auto":
            return "cuda" if torch_module.cuda.is_available() else "cpu"
        if configured == "cuda" and not torch_module.cuda.is_available():
            raise RuntimeError("AI_DEVICE=cuda nhưng CUDA không khả dụng")
        if configured not in {"cpu", "cuda"}:
            raise ModelConfigurationError("AI_DEVICE chỉ nhận auto, cpu hoặc cuda")
        return configured

    def moderate(self, text: str) -> ModerationResponse:
        normalized_text = unicodedata.normalize("NFC", text).strip()
        token_ids = self._tokenizer.encode(normalized_text, add_special_tokens=False)
        if not token_ids:
            raise ValueError("Nội dung không tạo được token hợp lệ")

        chunks = [
            token_ids[offset : offset + self._payload_length]
            for offset in range(0, len(token_ids), self._payload_length)
        ]
        with self._inference_lock, self._torch.inference_mode():
            predictions = [self._predict_chunk(chunk) for chunk in chunks]
        selected = aggregate_predictions(predictions)
        return ModerationResponse(
            label=selected.label,
            confidence=selected.confidence,
            scores=ModerationScores(**selected.scores),
        )

    def _predict_chunk(self, token_ids: list[int]) -> ChunkPrediction:
        prepared = self._tokenizer.prepare_for_model(
            token_ids,
            add_special_tokens=True,
            padding=False,
            truncation=False,
            return_attention_mask=True,
            return_tensors="pt",
        )
        input_ids = prepared["input_ids"]
        attention_mask = prepared["attention_mask"]
        # Slow PhoBERT tokenizer trả tensor 1 chiều khi prepare trực tiếp từ token IDs.
        if input_ids.ndim == 1:
            input_ids = input_ids.unsqueeze(0)
            attention_mask = attention_mask.unsqueeze(0)
        input_ids = input_ids.to(self._device)
        attention_mask = attention_mask.to(self._device)
        logits = self._model(input_ids=input_ids, attention_mask=attention_mask)
        probabilities = self._torch.softmax(logits, dim=-1)[0].detach().cpu().tolist()
        if len(probabilities) != len(self._id2label) or any(
            not math.isfinite(score) or score < 0.0 or score > 1.0 for score in probabilities
        ):
            raise RuntimeError("Model trả probability không hợp lệ")
        scores = {self._id2label[index]: float(score) for index, score in enumerate(probabilities)}
        label = max(scores, key=scores.__getitem__)
        return ChunkPrediction(label=label, confidence=scores[label], scores=scores)
