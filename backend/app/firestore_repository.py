from fastapi import HTTPException, status

from .auth import CurrentUser
from .config import get_settings
from .firebase_client import get_firestore_client
from .schemas import FlowerInfo, PredictionItem

MAX_HISTORY_ITEMS = 50
DEFAULT_USER_ROLE = "user"


def _model_to_dict(model) -> dict:
    if hasattr(model, "model_dump"):
        return model.model_dump()
    return model.dict()


def _timestamp_to_iso(value) -> str | None:
    if hasattr(value, "isoformat"):
        return value.isoformat()
    if isinstance(value, str):
        return value
    return None


def _clean_display_name(value: str | None) -> str | None:
    if not value:
        return None
    normalized = " ".join(value.strip().split())
    return normalized or None


def _history_payload_from_doc(doc) -> dict:
    data = doc.to_dict() or {}
    created_at = data.get("createdAt")
    if hasattr(created_at, "isoformat"):
        created_at = created_at.isoformat()
    image_url = data.get("imageUrl")
    if not isinstance(image_url, str) or not image_url.strip():
        image_url = None

    return {
        "id": doc.id,
        "predictedFlowerId": data.get("predictedFlowerId"),
        "displayName": data.get("displayName") or "Bilinmeyen cicek",
        "modelLabel": data.get("modelLabel") or "",
        "classId": int(data.get("classId") or 0),
        "confidence": float(data.get("confidence") or 0),
        "lowConfidence": bool(data.get("lowConfidence") or False),
        "imageUrl": image_url,
        "topPredictions": data.get("topPredictions") or [],
        "createdAt": created_at,
    }


class FirestoreRepository:
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

    def get_flower(self, flower_id: str | None) -> FlowerInfo | None:
        if not self.is_enabled or not flower_id:
            return None

        doc = self._client().collection("flowers").document(flower_id).get()
        if not doc.exists:
            return None

        data = doc.to_dict() or {}
        data["id"] = data.get("id") or flower_id
        return FlowerInfo(**data)

    def _profile_payload_from_data(
        self,
        *,
        uid: str,
        user: CurrentUser,
        data: dict,
    ) -> dict:
        return {
            "uid": data.get("uid") or uid,
            "email": data.get("email") or user.email,
            "displayName": _clean_display_name(data.get("displayName")),
            "role": data.get("role") or DEFAULT_USER_ROLE,
            "predictionCount": int(data.get("predictionCount") or 0),
            "createdAt": _timestamp_to_iso(data.get("createdAt")),
            "updatedAt": _timestamp_to_iso(data.get("updatedAt")),
            "lastActiveAt": _timestamp_to_iso(data.get("lastActiveAt")),
        }

    def _local_profile_payload(
        self,
        user: CurrentUser,
        display_name: str | None = None,
    ) -> dict:
        return {
            "uid": user.uid,
            "email": user.email,
            "displayName": _clean_display_name(display_name or user.display_name),
            "role": DEFAULT_USER_ROLE,
            "predictionCount": 0,
            "createdAt": None,
            "updatedAt": None,
            "lastActiveAt": None,
        }

    def get_user_profile(self, user: CurrentUser) -> dict:
        if not self.is_enabled:
            return self._local_profile_payload(user)

        client = self._client()
        from firebase_admin import firestore

        doc_ref = client.collection("users").document(user.uid)
        try:
            doc = doc_ref.get()
            if not doc.exists:
                doc_ref.set(
                    {
                        "uid": user.uid,
                        "email": user.email,
                        "displayName": _clean_display_name(user.display_name),
                        "role": DEFAULT_USER_ROLE,
                        "predictionCount": 0,
                        "createdAt": firestore.SERVER_TIMESTAMP,
                        "updatedAt": firestore.SERVER_TIMESTAMP,
                        "lastActiveAt": firestore.SERVER_TIMESTAMP,
                    }
                )
                doc = doc_ref.get()
            else:
                data = doc.to_dict() or {}
                update_data = {
                    "email": user.email,
                    "lastActiveAt": firestore.SERVER_TIMESTAMP,
                }
                if not data.get("displayName") and user.display_name:
                    update_data["displayName"] = _clean_display_name(user.display_name)
                doc_ref.set(update_data, merge=True)
                doc = doc_ref.get()
        except HTTPException:
            raise
        except Exception as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="User profile is not available.",
            ) from exc

        return self._profile_payload_from_data(
            uid=user.uid,
            user=user,
            data=doc.to_dict() or {},
        )

    def update_user_profile(self, user: CurrentUser, display_name: str) -> dict:
        cleaned_display_name = _clean_display_name(display_name)
        if not cleaned_display_name:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Display name is required.",
            )

        if not self.is_enabled:
            return self._local_profile_payload(user, cleaned_display_name)

        client = self._client()
        from firebase_admin import firestore

        doc_ref = client.collection("users").document(user.uid)
        try:
            doc = doc_ref.get()
            if not doc.exists:
                doc_ref.set(
                    {
                        "uid": user.uid,
                        "email": user.email,
                        "displayName": cleaned_display_name,
                        "role": DEFAULT_USER_ROLE,
                        "predictionCount": 0,
                        "createdAt": firestore.SERVER_TIMESTAMP,
                        "updatedAt": firestore.SERVER_TIMESTAMP,
                        "lastActiveAt": firestore.SERVER_TIMESTAMP,
                    }
                )
            else:
                doc_ref.set(
                    {
                        "email": user.email,
                        "displayName": cleaned_display_name,
                        "updatedAt": firestore.SERVER_TIMESTAMP,
                        "lastActiveAt": firestore.SERVER_TIMESTAMP,
                    },
                    merge=True,
                )
            doc = doc_ref.get()
        except HTTPException:
            raise
        except Exception as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="User profile is not available.",
            ) from exc

        return self._profile_payload_from_data(
            uid=user.uid,
            user=user,
            data=doc.to_dict() or {},
        )

    def record_prediction_for_user(self, user: CurrentUser) -> None:
        if not self.is_enabled:
            return

        client = self._client()
        from firebase_admin import firestore

        doc_ref = client.collection("users").document(user.uid)
        try:
            doc = doc_ref.get()
            if not doc.exists:
                doc_ref.set(
                    {
                        "uid": user.uid,
                        "email": user.email,
                        "displayName": _clean_display_name(user.display_name),
                        "role": DEFAULT_USER_ROLE,
                        "predictionCount": 1,
                        "createdAt": firestore.SERVER_TIMESTAMP,
                        "updatedAt": firestore.SERVER_TIMESTAMP,
                        "lastActiveAt": firestore.SERVER_TIMESTAMP,
                    }
                )
                return

            doc_ref.set(
                {
                    "email": user.email,
                    "predictionCount": firestore.Increment(1),
                    "updatedAt": firestore.SERVER_TIMESTAMP,
                    "lastActiveAt": firestore.SERVER_TIMESTAMP,
                },
                merge=True,
            )
        except Exception:
            return

    def create_prediction_history(
        self,
        user: CurrentUser,
        best_prediction: PredictionItem,
        top_predictions: list[PredictionItem],
        low_confidence: bool,
        image_url: str | None = None,
    ) -> str | None:
        if not self.is_enabled:
            return None

        client = self._client()

        from firebase_admin import firestore

        doc_ref = client.collection("predictionHistory").document()
        doc_ref.set(
            {
                "userId": user.uid,
                "userEmail": user.email,
                "predictedFlowerId": best_prediction.flowerId,
                "displayName": best_prediction.displayName,
                "modelLabel": best_prediction.modelLabel,
                "classId": best_prediction.classId,
                "confidence": best_prediction.confidence,
                "lowConfidence": low_confidence,
                "imageUrl": image_url or "",
                "topPredictions": [
                    _model_to_dict(prediction) for prediction in top_predictions
                ],
                "source": "mobile",
                "createdAt": firestore.SERVER_TIMESTAMP,
            }
        )
        return doc_ref.id

    def update_prediction_history_image_url(
        self,
        user: CurrentUser,
        prediction_id: str | None,
        image_url: str | None,
    ) -> None:
        if not self.is_enabled or not prediction_id or not image_url:
            return

        try:
            doc_ref = self._client().collection("predictionHistory").document(prediction_id)
            doc = doc_ref.get()
            if not doc.exists:
                return

            data = doc.to_dict() or {}
            if data.get("userId") != user.uid:
                return

            doc_ref.set({"imageUrl": image_url}, merge=True)
        except Exception:
            return

    def list_prediction_history(
        self,
        user: CurrentUser,
        limit: int = MAX_HISTORY_ITEMS,
    ) -> list[dict]:
        if not self.is_enabled:
            return []

        from google.cloud.firestore_v1.base_query import FieldFilter
        from google.api_core.exceptions import FailedPrecondition
        from firebase_admin import firestore

        try:
            bounded_limit = max(1, min(limit, MAX_HISTORY_ITEMS))
            query = self._client().collection("predictionHistory").where(
                filter=FieldFilter("userId", "==", user.uid)
            )
            try:
                docs = list(query.order_by(
                    "createdAt",
                    direction=firestore.Query.DESCENDING,
                ).limit(bounded_limit).stream())
            except FailedPrecondition:
                docs = list(query.stream())

            history_items: list[dict] = []
            for doc in docs:
                history_items.append(_history_payload_from_doc(doc))
        except HTTPException:
            raise
        except Exception as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Prediction history is not available.",
            ) from exc

        return sorted(
            history_items,
            key=lambda item: item.get("createdAt") or "",
            reverse=True,
        )[:bounded_limit]

    def get_prediction_history_item(
        self,
        user: CurrentUser,
        prediction_id: str,
    ) -> dict:
        if not self.is_enabled:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Prediction history item was not found.",
            )

        try:
            doc_ref = self._client().collection("predictionHistory").document(prediction_id)
            doc = doc_ref.get()
        except HTTPException:
            raise
        except Exception as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Prediction history is not available.",
            ) from exc

        if not doc.exists:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Prediction history item was not found.",
            )

        data = doc.to_dict() or {}
        if data.get("userId") != user.uid:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="You can only view your own prediction history.",
            )

        return _history_payload_from_doc(doc)

    def delete_prediction_history_item(self, user: CurrentUser, prediction_id: str) -> int:
        if not self.is_enabled:
            return 0

        try:
            doc_ref = self._client().collection("predictionHistory").document(prediction_id)
            doc = doc_ref.get()
        except HTTPException:
            raise
        except Exception as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Prediction history is not available.",
            ) from exc

        if not doc.exists:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Prediction history item was not found.",
            )

        data = doc.to_dict() or {}
        if data.get("userId") != user.uid:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="You can only delete your own prediction history.",
            )

        try:
            doc_ref.delete()
        except Exception as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Prediction history is not available.",
            ) from exc
        return 1

    def delete_prediction_history(self, user: CurrentUser) -> int:
        if not self.is_enabled:
            return 0

        client = self._client()
        from google.cloud.firestore_v1.base_query import FieldFilter

        try:
            docs = client.collection("predictionHistory").where(
                filter=FieldFilter("userId", "==", user.uid)
            ).stream()

            deleted_count = 0
            batch = client.batch()
            for doc in docs:
                batch.delete(doc.reference)
                deleted_count += 1

                if deleted_count % 450 == 0:
                    batch.commit()
                    batch = client.batch()

            if deleted_count % 450:
                batch.commit()
        except HTTPException:
            raise
        except Exception as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Prediction history is not available.",
            ) from exc

        return deleted_count
