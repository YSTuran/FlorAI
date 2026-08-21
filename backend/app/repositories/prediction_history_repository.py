from fastapi import HTTPException, status

from ..auth import CurrentUser
from ..schemas import PredictionItem
from .common import FirestoreRepositoryBase, MAX_HISTORY_ITEMS, model_to_dict


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


class PredictionHistoryRepository(FirestoreRepositoryBase):
    def create_id(self) -> str | None:
        if not self.is_enabled:
            return None

        return self._client().collection("predictionHistory").document().id

    def create(
        self,
        user: CurrentUser,
        best_prediction: PredictionItem,
        top_predictions: list[PredictionItem],
        low_confidence: bool,
        prediction_id: str | None = None,
        image_url: str | None = None,
    ) -> str | None:
        if not self.is_enabled:
            return None

        from firebase_admin import firestore

        doc_ref = self._client().collection("predictionHistory").document(prediction_id)
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
                    model_to_dict(prediction) for prediction in top_predictions
                ],
                "source": "mobile",
                "createdAt": firestore.SERVER_TIMESTAMP,
            }
        )
        return doc_ref.id

    def list(
        self,
        user: CurrentUser,
        limit: int = MAX_HISTORY_ITEMS,
    ) -> list[dict]:
        if not self.is_enabled:
            return []

        from firebase_admin import firestore
        from google.api_core.exceptions import FailedPrecondition
        from google.cloud.firestore_v1.base_query import FieldFilter

        try:
            bounded_limit = max(1, min(limit, MAX_HISTORY_ITEMS))
            query = self._client().collection("predictionHistory").where(
                filter=FieldFilter("userId", "==", user.uid)
            )
            try:
                docs = list(
                    query.order_by(
                        "createdAt",
                        direction=firestore.Query.DESCENDING,
                    )
                    .limit(bounded_limit)
                    .stream()
                )
            except FailedPrecondition:
                docs = list(query.stream())

            history_items = [_history_payload_from_doc(doc) for doc in docs]
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

    def get(self, user: CurrentUser, prediction_id: str) -> dict:
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

        self._ensure_owned_doc(doc, user, action="view")
        return _history_payload_from_doc(doc)

    def delete_item(self, user: CurrentUser, prediction_id: str) -> int:
        if not self.is_enabled:
            return 0

        try:
            doc_ref = self._client().collection("predictionHistory").document(prediction_id)
            doc = doc_ref.get()
            self._ensure_owned_doc(doc, user, action="delete")
            doc_ref.delete()
        except HTTPException:
            raise
        except Exception as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Prediction history is not available.",
            ) from exc

        return 1

    def delete_all(self, user: CurrentUser) -> int:
        if not self.is_enabled:
            return 0

        from google.cloud.firestore_v1.base_query import FieldFilter

        try:
            client = self._client()
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

    def update_image_url(
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

    def _ensure_owned_doc(self, doc, user: CurrentUser, action: str) -> None:
        if not doc.exists:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Prediction history item was not found.",
            )

        data = doc.to_dict() or {}
        if data.get("userId") != user.uid:
            verb = "view" if action == "view" else "delete"
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"You can only {verb} your own prediction history.",
            )
