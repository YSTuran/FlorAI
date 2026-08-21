from fastapi import APIRouter, Depends, Request

from ..config import get_settings
from ..dependencies import get_classifier
from ..model_service import FlowerClassifier
from ..schemas import AppInfoResponse, HealthResponse


router = APIRouter()


@router.get("/", response_model=AppInfoResponse)
async def root(
    request: Request,
    classifier: FlowerClassifier = Depends(get_classifier),
) -> AppInfoResponse:
    settings = get_settings()
    return AppInfoResponse(
        appName=settings.app_name,
        version=request.app.version,
        description=(
            "FlorAI is a FastAPI backend that identifies flower images and "
            "returns botanical information for supported classes."
        ),
        modelLoaded=classifier.is_loaded,
        classCount=len(classifier.names),
        classes=list(classifier.names.values()),
        firestoreEnabled=settings.firestore_enabled,
        storageEnabled=bool(settings.firebase_storage_bucket),
        endpoints={
            "health": "GET /health",
            "predict": "POST /predict",
            "currentUser": "GET /users/me",
            "updateCurrentUser": "PUT /users/me",
            "deleteCurrentUser": "DELETE /users/me",
            "predictionHistory": "GET /prediction-history",
            "predictionHistoryDetail": "GET /prediction-history/{prediction_id}",
            "deletePrediction": "DELETE /prediction-history/{prediction_id}",
            "deleteAllPredictions": "DELETE /prediction-history",
        },
    )


@router.get("/health", response_model=HealthResponse)
async def health(
    classifier: FlowerClassifier = Depends(get_classifier),
) -> HealthResponse:
    settings = get_settings()
    return HealthResponse(
        status="ok",
        modelLoaded=classifier.is_loaded,
        classCount=len(classifier.names),
        classes=list(classifier.names.values()),
        firestoreEnabled=settings.firestore_enabled,
        storageEnabled=bool(settings.firebase_storage_bucket),
    )
