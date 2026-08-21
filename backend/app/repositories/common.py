from fastapi import HTTPException, status

from ..config import get_settings
from ..firebase_client import get_firestore_client


MAX_HISTORY_ITEMS = 50
DEFAULT_USER_ROLE = "user"


class FirestoreRepositoryBase:
    @property
    def is_enabled(self) -> bool:
        return get_settings().firestore_enabled

    def _client(self):
        try:
            return get_firestore_client()
        except Exception as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Firestore is not available.",
            ) from exc


def model_to_dict(model) -> dict:
    if hasattr(model, "model_dump"):
        return model.model_dump()
    return model.dict()


def timestamp_to_iso(value) -> str | None:
    if hasattr(value, "isoformat"):
        return value.isoformat()
    if isinstance(value, str):
        return value
    return None


def clean_display_name(value: str | None) -> str | None:
    if not value:
        return None
    normalized = " ".join(value.strip().split())
    return normalized or None
