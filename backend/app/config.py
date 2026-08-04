import os
from dataclasses import dataclass
from functools import lru_cache
from pathlib import Path


@dataclass(frozen=True)
class Settings:
    app_name: str
    model_path: Path
    confidence_threshold: float
    top_k: int
    max_image_size_mb: int
    firebase_auth_required: bool
    require_verified_email: bool
    firebase_credentials_json: str | None
    firebase_service_account_path: str | None


def _bool_env(name: str, default: bool) -> bool:
    value = os.getenv(name)
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "on"}


@lru_cache
def get_settings() -> Settings:
    backend_root = Path(__file__).resolve().parents[1]
    model_path = Path(os.getenv("MODEL_PATH", backend_root / "models" / "model.pt"))
    if not model_path.is_absolute():
        model_path = backend_root / model_path

    return Settings(
        app_name=os.getenv("APP_NAME", "FlorAI Backend"),
        model_path=model_path,
        confidence_threshold=float(os.getenv("PREDICTION_CONFIDENCE_THRESHOLD", "0.60")),
        top_k=int(os.getenv("PREDICTION_TOP_K", "5")),
        max_image_size_mb=int(os.getenv("MAX_IMAGE_SIZE_MB", "8")),
        firebase_auth_required=_bool_env("FIREBASE_AUTH_REQUIRED", False),
        require_verified_email=_bool_env("REQUIRE_VERIFIED_EMAIL", True),
        firebase_credentials_json=os.getenv("FIREBASE_CREDENTIALS_JSON"),
        firebase_service_account_path=os.getenv("FIREBASE_SERVICE_ACCOUNT_PATH")
        or os.getenv("GOOGLE_APPLICATION_CREDENTIALS"),
    )
