from pathlib import Path
from urllib.parse import quote
from uuid import uuid4

from fastapi import HTTPException, status

from .auth import CurrentUser
from .config import get_settings
from .firebase_client import get_storage_bucket


CONTENT_TYPE_TO_EXTENSION = {
    "image/jpeg": "jpg",
    "image/jpg": "jpg",
    "image/png": "png",
    "image/webp": "webp",
    "image/heic": "heic",
    "image/heif": "heif",
}


def _resolve_extension(content_type: str | None, filename: str | None) -> str:
    content_type_extension = CONTENT_TYPE_TO_EXTENSION.get((content_type or "").lower())
    if content_type_extension:
        return content_type_extension

    suffix = Path(filename or "").suffix.lower().lstrip(".")
    if suffix in {"jpg", "jpeg", "png", "webp", "heic", "heif"}:
        return "jpg" if suffix == "jpeg" else suffix

    return "jpg"


class StorageService:
    @property
    def is_enabled(self) -> bool:
        return bool(get_settings().firebase_storage_bucket)

    def upload_prediction_image(
        self,
        *,
        user: CurrentUser,
        prediction_id: str | None,
        image_bytes: bytes,
        content_type: str | None,
        filename: str | None,
    ) -> str | None:
        if not self.is_enabled or not prediction_id:
            return None

        try:
            bucket = get_storage_bucket()
            extension = _resolve_extension(content_type, filename)
            object_path = f"prediction-images/{user.uid}/{prediction_id}.{extension}"
            token = str(uuid4())
            blob = bucket.blob(object_path)
            blob.metadata = {"firebaseStorageDownloadTokens": token}
            blob.upload_from_string(
                image_bytes,
                content_type=content_type or "image/jpeg",
            )

            encoded_path = quote(blob.name, safe="")
            return (
                f"https://firebasestorage.googleapis.com/v0/b/{bucket.name}/o/"
                f"{encoded_path}?alt=media&token={token}"
            )
        except Exception as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Prediction image could not be uploaded.",
            ) from exc

    def delete_prediction_image(self, *, user: CurrentUser, prediction_id: str) -> None:
        if not self.is_enabled:
            return

        try:
            bucket = get_storage_bucket()
            prefix = f"prediction-images/{user.uid}/{prediction_id}."
            for blob in bucket.list_blobs(prefix=prefix):
                blob.delete()
        except Exception:
            return

    def delete_user_prediction_images(self, *, user: CurrentUser) -> None:
        if not self.is_enabled:
            return

        try:
            bucket = get_storage_bucket()
            prefix = f"prediction-images/{user.uid}/"
            for blob in bucket.list_blobs(prefix=prefix):
                blob.delete()
        except Exception:
            return
