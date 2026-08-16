from contextlib import nullcontext

import pytest

from app.inference import (
    ChunkPrediction,
    ModelConfigurationError,
    aggregate_predictions,
    validate_label_mapping,
    PhoBertHsdEngine,
)


def prediction(label: str, confidence: float) -> ChunkPrediction:
    scores = {"CLEAN": 0.01, "OFFENSIVE": 0.01, "HATE": 0.01}
    scores[label] = confidence
    return ChunkPrediction(label=label, confidence=confidence, scores=scores)


def test_uses_verified_checkpoint_label_mapping() -> None:
    assert validate_label_mapping({"0": "CLEAN", "1": "OFFENSIVE", "2": "HATE"}) == {
        0: "CLEAN",
        1: "OFFENSIVE",
        2: "HATE",
    }


def test_unknown_mapping_fails_instead_of_guessing_indices() -> None:
    with pytest.raises(ModelConfigurationError):
        validate_label_mapping({0: "LABEL_0", 1: "LABEL_1", 2: "LABEL_2"})


def test_violation_in_final_chunk_is_not_lost() -> None:
    selected = aggregate_predictions(
        [prediction("CLEAN", 0.99), prediction("CLEAN", 0.95), prediction("HATE", 0.70)]
    )
    assert selected.label == "HATE"


def test_engine_actually_evaluates_violation_in_final_token_chunk() -> None:
    class FakeTokenizer:
        def num_special_tokens_to_add(self, pair: bool = False) -> int:
            return 2

        def encode(self, text: str, add_special_tokens: bool = False) -> list[int]:
            return [10, 11, 12, 999]

    class FakeTorch:
        @staticmethod
        def inference_mode():
            return nullcontext()

    class InspectingEngine(PhoBertHsdEngine):
        def _predict_chunk(self, token_ids: list[int]) -> ChunkPrediction:
            return prediction("HATE", 0.8) if 999 in token_ids else prediction("CLEAN", 0.99)

    engine = InspectingEngine(
        FakeTokenizer(), object(), FakeTorch(), {0: "CLEAN", 1: "OFFENSIVE", 2: "HATE"}, "cpu", 4
    )

    assert engine.moderate("nội dung dài").label == "HATE"


def test_severity_wins_before_confidence() -> None:
    selected = aggregate_predictions([prediction("OFFENSIVE", 0.55), prediction("CLEAN", 0.999)])
    assert selected.label == "OFFENSIVE"
