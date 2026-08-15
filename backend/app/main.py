from contextlib import asynccontextmanager

from fastapi import Depends, FastAPI, File, HTTPException, Request, UploadFile, status

from .auth import CurrentUser, get_current_user
from .config import get_settings
from .firestore_repository import FirestoreRepository
from .flower_catalog import get_flower_by_model_label
from .model_service import FlowerClassifier
from .schemas import (
    AppInfoResponse,
    DeleteResponse,
    HealthResponse,
    PredictResponse,
    PredictionHistoryItem,
    PredictionHistoryResponse,
    PredictionResult,
)


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings = get_settings()
    classifier = FlowerClassifier(
        model_path=settings.model_path,
        confidence_threshold=settings.confidence_threshold,
    )
    classifier.load()
    app.state.classifier = classifier
    app.state.firestore_repository = FirestoreRepository()
    yield


app = FastAPI(title=get_settings().app_name, version="0.1.0", lifespan=lifespan)


def get_classifier(request: Request) -> FlowerClassifier:
    return request.app.state.classifier


def get_firestore_repository(request: Request) -> FirestoreRepository:
    return request.app.state.firestore_repository


@app.get("/", response_model=AppInfoResponse)
async def root(classifier: FlowerClassifier = Depends(get_classifier)) -> AppInfoResponse:
    settings = get_settings()
    return AppInfoResponse(
        appName=settings.app_name,
        version=app.version,
        description=(
            "FlorAI is a FastAPI backend that identifies flower images and "
            "returns botanical information for supported classes."
        ),
        modelLoaded=classifier.is_loaded,
        classCount=len(classifier.names),
        classes=list(classifier.names.values()),
        firestoreEnabled=settings.firestore_enabled,
        endpoints={
            "health": "GET /health",
            "predict": "POST /predict",
            "predictionHistory": "GET /prediction-history",
            "deletePrediction": "DELETE /prediction-history/{prediction_id}",
            "deleteAllPredictions": "DELETE /prediction-history",
        },
    )


@app.get("/health", response_model=HealthResponse)
async def health(classifier: FlowerClassifier = Depends(get_classifier)) -> HealthResponse:
    settings = get_settings()
    return HealthResponse(
        status="ok",
        modelLoaded=classifier.is_loaded,
        classCount=len(classifier.names),
        classes=list(classifier.names.values()),
        firestoreEnabled=settings.firestore_enabled,
    )


@app.post("/predict", response_model=PredictResponse)
async def predict(
    image: UploadFile = File(...),
    current_user: CurrentUser = Depends(get_current_user),
    classifier: FlowerClassifier = Depends(get_classifier),
    firestore_repository: FirestoreRepository = Depends(get_firestore_repository),
) -> PredictResponse:
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

    predictions = classifier.predict(image_bytes=image_bytes, top_k=settings.top_k)
    best_prediction = predictions[0]
    flower = firestore_repository.get_flower(best_prediction.flowerId)
    if flower is None:
        flower = get_flower_by_model_label(best_prediction.modelLabel)

    low_confidence = best_prediction.confidence < settings.confidence_threshold
    prediction_id = firestore_repository.create_prediction_history(
        user=current_user,
        best_prediction=best_prediction,
        top_predictions=predictions,
        low_confidence=low_confidence,
    )

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


@app.get("/prediction-history", response_model=PredictionHistoryResponse)
async def list_prediction_history(
    current_user: CurrentUser = Depends(get_current_user),
    firestore_repository: FirestoreRepository = Depends(get_firestore_repository),
) -> PredictionHistoryResponse:
    items = firestore_repository.list_prediction_history(current_user)
    return PredictionHistoryResponse(
        items=[PredictionHistoryItem(**item) for item in items]
    )


@app.delete("/prediction-history/{prediction_id}", response_model=DeleteResponse)
async def delete_prediction_history_item(
    prediction_id: str,
    current_user: CurrentUser = Depends(get_current_user),
    firestore_repository: FirestoreRepository = Depends(get_firestore_repository),
) -> DeleteResponse:
    deleted_count = firestore_repository.delete_prediction_history_item(
        user=current_user,
        prediction_id=prediction_id,
    )
    return DeleteResponse(deletedCount=deleted_count)


@app.delete("/prediction-history", response_model=DeleteResponse)
async def delete_prediction_history(
    current_user: CurrentUser = Depends(get_current_user),
    firestore_repository: FirestoreRepository = Depends(get_firestore_repository),
) -> DeleteResponse:
    deleted_count = firestore_repository.delete_prediction_history(current_user)
    return DeleteResponse(deletedCount=deleted_count)
