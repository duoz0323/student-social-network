from fastapi.testclient import TestClient

import pytest

from app.inference import ModelConfigurationError
from app.main import create_app
from app.schemas import ModerationResponse, ModerationScores
from app.settings import Settings


class FakeEngine:
    def __init__(self, label: str = "CLEAN") -> None:
        self.label = label

    def moderate(self, text: str) -> ModerationResponse:
        scores = {
            "CLEAN": ModerationScores(CLEAN=0.9, OFFENSIVE=0.08, HATE=0.02),
            "OFFENSIVE": ModerationScores(CLEAN=0.1, OFFENSIVE=0.8, HATE=0.1),
            "HATE": ModerationScores(CLEAN=0.01, OFFENSIVE=0.04, HATE=0.95),
        }[self.label]
        return ModerationResponse(label=self.label, confidence=getattr(scores, self.label), scores=scores)


def client_for(label: str = "CLEAN") -> TestClient:
    return TestClient(create_app(Settings(max_input_characters=100), lambda _: FakeEngine(label)))


def test_health_ready_and_clean_contract() -> None:
    with client_for() as client:
        assert client.get("/health").json() == {"status": "ok"}
        assert client.get("/ready").json() == {"status": "ready"}
        response = client.post("/v1/moderation", json={"text": "Hôm nay trời đẹp"})
        assert response.status_code == 200
        assert response.json()["label"] == "CLEAN"


def test_offensive_and_hate_contracts() -> None:
    for label in ("OFFENSIVE", "HATE"):
        with client_for(label) as client:
            response = client.post("/v1/moderation", json={"text": "Nội dung kiểm thử"})
            assert response.status_code == 200
            assert response.json()["label"] == label


def test_rejects_blank_extra_and_too_long_input() -> None:
    with client_for() as client:
        assert client.post("/v1/moderation", json={"text": "   "}).status_code == 422
        assert client.post("/v1/moderation", json={"text": "ok", "secret": "x"}).status_code == 422
        assert client.post("/v1/moderation", json={"text": "x" * 101}).status_code == 422


def test_not_ready_when_model_loading_fails() -> None:
    def failed_factory(_: Settings) -> FakeEngine:
        raise OSError("model unavailable")

    with TestClient(create_app(Settings(), failed_factory)) as client:
        assert client.get("/health").status_code == 200
        assert client.get("/ready").status_code == 503
        assert client.post("/v1/moderation", json={"text": "test"}).status_code == 503


def test_model_is_loaded_only_once_per_process_lifespan() -> None:
    calls = 0

    def counting_factory(_: Settings) -> FakeEngine:
        nonlocal calls
        calls += 1
        return FakeEngine()

    with TestClient(create_app(Settings(), counting_factory)) as client:
        client.post("/v1/moderation", json={"text": "lần một"})
        client.post("/v1/moderation", json={"text": "lần hai"})
    assert calls == 1


def test_invalid_checkpoint_contract_fails_startup() -> None:
    def invalid_factory(_: Settings) -> FakeEngine:
        raise ModelConfigurationError("invalid mapping")

    with pytest.raises(ModelConfigurationError):
        with TestClient(create_app(Settings(), invalid_factory)):
            pass
