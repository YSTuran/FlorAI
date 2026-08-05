from fastapi import HTTPException, status

from .auth import CurrentUser
from .config import get_settings
from .firebase_client import get_firestore_client
from .schemas import FlowerInfo, PredictionItem


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
                "topPredictions": [
                    _model_to_dict(prediction) for prediction in top_predictions
                ],
                "source": "mobile",
                "createdAt": firestore.SERVER_TIMESTAMP,
            }
        )
        return doc_ref.id
