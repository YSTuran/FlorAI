from contextlib import asynccontextmanager

from fastapi import Depends, FastAPI, File, HTTPException, Request, UploadFile, status

from .auth import CurrentUser, get_current_user
from .config import get_settings
from .flower_catalog import get_flower_by_model_label
from .model_service import FlowerClassifier
from .schemas import HealthResponse, PredictResponse


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings = get_settings()
    classifier = FlowerClassifier(
        model_path=settings.model_path,
        confidence_threshold=settings.confidence_threshold,
    )
    classifier.load()
    app.state.classifier = classifier
    yield


app = FastAPI(title=get_settings().app_name, version="0.1.0", lifespan=lifespan)


def get_classifier(request: Request) -> FlowerClassifier:
    return request.app.state.classifier


@app.get("/health", response_model=HealthResponse)
async def health(classifier: FlowerClassifier = Depends(get_classifier)) -> HealthResponse:
    return HealthResponse(
        status="ok",
        modelLoaded=classifier.is_loaded,
        classCount=len(classifier.names),
        classes=list(classifier.names.values()),
    )


@app.post("/predict", response_model=PredictResponse)
async def predict(
    image: UploadFile = File(...),
    current_user: CurrentUser = Depends(get_current_user),
    classifier: FlowerClassifier = Depends(get_classifier),
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
    flower = get_flower_by_model_label(best_prediction.modelLabel)

    return PredictResponse(
        prediction=best_prediction,
        topPredictions=predictions,
        flower=flower,
        lowConfidence=best_prediction.confidence < settings.confidence_threshold,
        userId=current_user.uid,
    )
