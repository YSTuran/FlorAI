from fastapi import Depends, Request

from .model_service import FlowerClassifier
from .repositories.flower_repository import FlowerRepository
from .repositories.prediction_history_repository import PredictionHistoryRepository
from .repositories.user_repository import UserRepository
from .services.firebase_auth_service import FirebaseAuthService
from .services.prediction_history_service import PredictionHistoryService
from .services.prediction_service import PredictionService
from .services.user_service import UserService
from .storage_service import StorageService


def get_classifier(request: Request) -> FlowerClassifier:
    return request.app.state.classifier


def get_flower_repository(request: Request) -> FlowerRepository:
    return request.app.state.flower_repository


def get_user_repository(request: Request) -> UserRepository:
    return request.app.state.user_repository


def get_prediction_history_repository(request: Request) -> PredictionHistoryRepository:
    return request.app.state.prediction_history_repository


def get_storage_service(request: Request) -> StorageService:
    return request.app.state.storage_service


def get_firebase_auth_service(request: Request) -> FirebaseAuthService:
    return request.app.state.firebase_auth_service


def get_prediction_service(
    classifier: FlowerClassifier = Depends(get_classifier),
    flower_repository: FlowerRepository = Depends(get_flower_repository),
    history_repository: PredictionHistoryRepository = Depends(
        get_prediction_history_repository
    ),
    user_repository: UserRepository = Depends(get_user_repository),
    storage_service: StorageService = Depends(get_storage_service),
) -> PredictionService:
    return PredictionService(
        classifier=classifier,
        flower_repository=flower_repository,
        history_repository=history_repository,
        user_repository=user_repository,
        storage_service=storage_service,
    )


def get_user_service(
    user_repository: UserRepository = Depends(get_user_repository),
    history_repository: PredictionHistoryRepository = Depends(
        get_prediction_history_repository
    ),
    storage_service: StorageService = Depends(get_storage_service),
    firebase_auth_service: FirebaseAuthService = Depends(get_firebase_auth_service),
) -> UserService:
    return UserService(
        user_repository=user_repository,
        history_repository=history_repository,
        storage_service=storage_service,
        firebase_auth_service=firebase_auth_service,
    )


def get_prediction_history_service(
    history_repository: PredictionHistoryRepository = Depends(
        get_prediction_history_repository
    ),
    user_repository: UserRepository = Depends(get_user_repository),
    storage_service: StorageService = Depends(get_storage_service),
) -> PredictionHistoryService:
    return PredictionHistoryService(
        history_repository=history_repository,
        user_repository=user_repository,
        storage_service=storage_service,
    )
