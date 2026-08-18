import os
from dataclasses import dataclass
from functools import lru_cache
from pathlib import Path


@dataclass(frozen=True)
class Settings:
    app_name: str
    backend_root: Path
    model_path: Path
    confidence_threshold: float
    top_k: int
    max_image_size_mb: int
    firebase_auth_required: bool
    require_verified_email: bool
    firestore_enabled: bool
    firebase_credentials_json: str | None
    firebase_service_account_path: str | None
    firebase_storage_bucket: str | None


def _bool_env(name: str, default: bool) -> bool:
    value = os.getenv(name)
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "on"}


def _load_dotenv(backend_root: Path) -> None:
    env_path = backend_root / ".env"
    if not env_path.exists():
        return

    try:
        from dotenv import load_dotenv

        load_dotenv(env_path, override=False)
        return
    except ImportError:
        pass

    for raw_line in env_path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        os.environ.setdefault(key.strip(), value.strip().strip("\"'"))


@lru_cache
def get_settings() -> Settings:
    backend_root = Path(__file__).resolve().parents[1]
    _load_dotenv(backend_root)

    model_path = Path(os.getenv("MODEL_PATH", backend_root / "models" / "model.pt"))
    if not model_path.is_absolute():
        model_path = backend_root / model_path

    firebase_service_account_path = (
        os.getenv("FIREBASE_SERVICE_ACCOUNT_PATH")
        or os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
    )
    if firebase_service_account_path:
        service_account_path = Path(firebase_service_account_path)
        if not service_account_path.is_absolute():
            service_account_path = backend_root / service_account_path
        firebase_service_account_path = str(service_account_path)

    return Settings(
        app_name=os.getenv("APP_NAME", "FlorAI Backend"),
        backend_root=backend_root,
        model_path=model_path,
        confidence_threshold=float(os.getenv("PREDICTION_CONFIDENCE_THRESHOLD", "0.60")),
        top_k=int(os.getenv("PREDICTION_TOP_K", "5")),
        max_image_size_mb=int(os.getenv("MAX_IMAGE_SIZE_MB", "8")),
        firebase_auth_required=_bool_env("FIREBASE_AUTH_REQUIRED", False),
        require_verified_email=_bool_env("REQUIRE_VERIFIED_EMAIL", True),
        firestore_enabled=_bool_env("FIRESTORE_ENABLED", False),
        firebase_credentials_json=os.getenv("FIREBASE_CREDENTIALS_JSON"),
        firebase_service_account_path=firebase_service_account_path,
        firebase_storage_bucket=os.getenv("FIREBASE_STORAGE_BUCKET"),
    )
