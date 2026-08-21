from fastapi import HTTPException, UploadFile, status

from ..auth import CurrentUser
from ..config import get_settings
from ..flower_catalog import get_flower_by_model_label
from ..model_service import FlowerClassifier
from ..repositories.flower_repository import FlowerRepository
from ..repositories.prediction_history_repository import PredictionHistoryRepository
from ..repositories.user_repository import UserRepository
from ..schemas import PredictResponse, PredictionResult
from ..storage_service import StorageService


class PredictionService:
    def __init__(
        self,
        *,
        classifier: FlowerClassifier,
        flower_repository: FlowerRepository,
        history_repository: PredictionHistoryRepository,
        user_repository: UserRepository,
        storage_service: StorageService,
    ) -> None:
        self._classifier = classifier
        self._flower_repository = flower_repository
        self._history_repository = history_repository
        self._user_repository = user_repository
        self._storage_service = storage_service

    async def predict(self, *, image: UploadFile, user: CurrentUser) -> PredictResponse:
        settings = get_settings()

        if image.content_type and not image.content_type.startswith("image/"):
            raise HTTPException(
                status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
                detail="Only image uploads are supported.",
            )

        image_bytes = await image.read()
        if not image_bytes:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Image file is empty.",
            )

        max_bytes = settings.max_image_size_mb * 1024 * 1024
        if len(image_bytes) > max_bytes:
            raise HTTPException(
                status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
                detail=f"Image must be smaller than {settings.max_image_size_mb} MB.",
            )

        predictions = self._classifier.predict(
            image_bytes=image_bytes,
            top_k=settings.top_k,
        )
        best_prediction = predictions[0]
        flower = self._flower_repository.get_flower(best_prediction.flowerId)
        if flower is None:
            flower = get_flower_by_model_label(best_prediction.modelLabel)

        low_confidence = best_prediction.confidence < settings.confidence_threshold
        prediction_id = self._history_repository.create_id()
        image_url = self._storage_service.upload_prediction_image(
            user=user,
            prediction_id=prediction_id,
            image_bytes=image_bytes,
            content_type=image.content_type,
            filename=image.filename,
        )

        try:
            prediction_id = self._history_repository.create(
                user=user,
                best_prediction=best_prediction,
                top_predictions=predictions,
                low_confidence=low_confidence,
                prediction_id=prediction_id,
                image_url=image_url,
            )
        except HTTPException:
            if prediction_id is not None:
                self._storage_service.delete_prediction_image(
                    user=user,
                    prediction_id=prediction_id,
                )
            raise

        self._user_repository.record_prediction(user)

        return PredictResponse(
            status="low_confidence" if low_confidence else "success",
            predictionId=prediction_id,
            result=PredictionResult(
                flowerId=best_prediction.flowerId,
                classId=best_prediction.classId,
                modelLabel=best_prediction.modelLabel,
                name=flower.commonName if flower else best_prediction.displayName,
                scientificName=flower.scientificName if flower else None,
                confidence=best_prediction.confidence,
                lowConfidence=low_confidence,
                height=flower.height if flower else None,
                habitats=flower.habitats if flower else [],
                bloomMonths=flower.bloomMonths if flower else [],
                details=flower.details if flower else None,
                extraFacts=flower.extraFacts if flower else [],
            ),
        )
