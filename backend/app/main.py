from contextlib import asynccontextmanager

from fastapi import FastAPI

from .api import history, predictions, root, users
from .config import get_settings
from .model_service import FlowerClassifier
from .repositories.flower_repository import FlowerRepository
from .repositories.prediction_history_repository import PredictionHistoryRepository
from .repositories.user_repository import UserRepository
from .storage_service import StorageService


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings = get_settings()
    classifier = FlowerClassifier(
        model_path=settings.model_path,
        confidence_threshold=settings.confidence_threshold,
    )
    classifier.load()

    app.state.classifier = classifier
    app.state.flower_repository = FlowerRepository()
    app.state.user_repository = UserRepository()
    app.state.prediction_history_repository = PredictionHistoryRepository()
    app.state.storage_service = StorageService()
    yield


app = FastAPI(title=get_settings().app_name, version="0.1.0", lifespan=lifespan)

app.include_router(root.router)
app.include_router(predictions.router)
app.include_router(users.router)
app.include_router(history.router)
