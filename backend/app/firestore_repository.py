from fastapi import HTTPException, status

from .auth import CurrentUser
from .config import get_settings
from .firebase_client import get_firestore_client
from .schemas import FlowerInfo, PredictionItem

MAX_HISTORY_ITEMS = 50


def _model_to_dict(model) -> dict:
    if hasattr(model, "model_dump"):
        return model.model_dump()
    return model.dict()


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

    def create_prediction_history(
        self,
        user: CurrentUser,
        best_prediction: PredictionItem,
        top_predictions: list[PredictionItem],
        low_confidence: bool,
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
                "imageUrl": "",
                "topPredictions": [
                    _model_to_dict(prediction) for prediction in top_predictions
                ],
                "source": "mobile",
                "createdAt": firestore.SERVER_TIMESTAMP,
            }
        )
        return doc_ref.id

    def list_prediction_history(self, user: CurrentUser) -> list[dict]:
        if not self.is_enabled:
            return []

        from google.cloud.firestore_v1.base_query import FieldFilter

        try:
            docs = self._client().collection("predictionHistory").where(
                filter=FieldFilter("userId", "==", user.uid)
            ).limit(MAX_HISTORY_ITEMS).stream()

            history_items: list[dict] = []
            for doc in docs:
                data = doc.to_dict() or {}
                created_at = data.get("createdAt")
                if hasattr(created_at, "isoformat"):
                    created_at = created_at.isoformat()
                image_url = data.get("imageUrl")
                if not isinstance(image_url, str) or not image_url.strip():
                    image_url = None

                history_items.append(
                    {
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
                )
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
        )

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
