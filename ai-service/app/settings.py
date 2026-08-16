from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path


MODEL_REVISION = "221aca47de6568d519eba61a94d7fdae3ca680ae"
BASE_MODEL_REVISION = "86cd7fd4c148980922ac11a2cf5e257f2ba639e1"


@dataclass(frozen=True)
class Settings:
    """Cấu hình runtime chỉ đọc từ môi trường, không chứa nội dung người dùng."""

    model_name: str = "visolex/phobert-v2-hsd"
    model_revision: str = MODEL_REVISION
    base_model_name: str = "vinai/phobert-base-v2"
    base_model_revision: str = BASE_MODEL_REVISION
    model_cache_dir: Path = Path(".cache/huggingface")
    device: str = "auto"
    max_sequence_length: int = 256
    max_input_characters: int = 20_000

    @classmethod
    def from_environment(cls) -> "Settings":
        return cls(
            model_name=os.getenv("AI_MODEL_NAME", cls.model_name),
            model_revision=os.getenv("AI_MODEL_REVISION", cls.model_revision),
            base_model_name=os.getenv("AI_BASE_MODEL_NAME", cls.base_model_name),
            base_model_revision=os.getenv("AI_BASE_MODEL_REVISION", cls.base_model_revision),
            model_cache_dir=Path(os.getenv("AI_MODEL_CACHE_DIR", str(cls.model_cache_dir))),
            device=os.getenv("AI_DEVICE", cls.device).strip().lower(),
            max_sequence_length=int(os.getenv("AI_MODEL_MAX_LENGTH", str(cls.max_sequence_length))),
            max_input_characters=int(os.getenv("AI_MAX_INPUT_CHARACTERS", str(cls.max_input_characters))),
        )
