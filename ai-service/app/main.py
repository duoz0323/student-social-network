from __future__ import annotations

import logging
from collections.abc import Callable
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, Request, status

from .inference import ModelConfigurationError, ModerationEngine, PhoBertHsdEngine
from .schemas import HealthResponse, ModerationRequest, ModerationResponse, ReadyResponse
from .settings import Settings


LOGGER = logging.getLogger("ai_service")


def create_app(
    settings: Settings | None = None,
    engine_factory: Callable[[Settings], ModerationEngine] = PhoBertHsdEngine.load,
) -> FastAPI:
    runtime_settings = settings or Settings.from_environment()

    @asynccontextmanager
    async def lifespan(app: FastAPI):
        app.state.engine = None
        app.state.load_error = None
        try:
            app.state.engine = engine_factory(runtime_settings)
            LOGGER.info("AI moderation model đã sẵn sàng")
        except ModelConfigurationError:
            # Sai label/kiến trúc là lỗi cấu hình không được phép chạy âm thầm.
            LOGGER.exception("Checkpoint moderation không đúng contract")
            raise
        except Exception as exception:  # noqa: BLE001 - giữ process sống để health/readiness phản ánh đúng trạng thái
            app.state.load_error = type(exception).__name__
            LOGGER.exception("Không thể nạp AI moderation model")
        yield
        app.state.engine = None

    app = FastAPI(title="Student Social Network AI Moderation", version="1.0.0", lifespan=lifespan)

    @app.get("/health", response_model=HealthResponse)
    def health() -> HealthResponse:
        return HealthResponse()

    @app.get("/ready", response_model=ReadyResponse)
    def ready(request: Request) -> ReadyResponse:
        if request.app.state.engine is None:
            raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Model chưa sẵn sàng")
        return ReadyResponse()

    @app.post("/v1/moderation", response_model=ModerationResponse)
    def moderate(payload: ModerationRequest, request: Request) -> ModerationResponse:
        if len(payload.text) > runtime_settings.max_input_characters:
            raise HTTPException(status_code=422, detail="Nội dung vượt giới hạn")
        engine: ModerationEngine | None = request.app.state.engine
        if engine is None:
            raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Model chưa sẵn sàng")
        try:
            return engine.moderate(payload.text)
        except ValueError as exception:
            raise HTTPException(status_code=422, detail=str(exception)) from exception
        except Exception as exception:  # noqa: BLE001 - không expose lỗi/model/raw text ra client
            LOGGER.exception("Inference moderation thất bại")
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Không thể phân loại nội dung",
            ) from exception

    return app


app = create_app()
