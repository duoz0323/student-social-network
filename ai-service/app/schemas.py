from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, field_validator


ModerationLabel = Literal["CLEAN", "OFFENSIVE", "HATE"]


class ModerationRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    text: str = Field(min_length=1)

    @field_validator("text")
    @classmethod
    def reject_blank_text(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("text không được để trống")
        return value


class ModerationScores(BaseModel):
    model_config = ConfigDict(extra="forbid")

    CLEAN: float = Field(ge=0.0, le=1.0)
    OFFENSIVE: float = Field(ge=0.0, le=1.0)
    HATE: float = Field(ge=0.0, le=1.0)


class ModerationResponse(BaseModel):
    model_config = ConfigDict(extra="forbid")

    label: ModerationLabel
    confidence: float = Field(ge=0.0, le=1.0)
    scores: ModerationScores


class HealthResponse(BaseModel):
    status: Literal["ok"] = "ok"


class ReadyResponse(BaseModel):
    status: Literal["ready"] = "ready"
