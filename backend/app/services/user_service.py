import logging
import time

from fastapi import HTTPException, status

from ..auth import CurrentUser
from ..config import get_settings
from ..repositories.prediction_history_repository import PredictionHistoryRepository
from ..repositories.user_repository import UserRepository
from ..schemas import DeleteResponse, UserProfile
from ..services.firebase_auth_service import FirebaseAuthService
from ..storage_service import StorageService


logger = logging.getLogger(__name__)


class UserService:
    def __init__(
        self,
        *,
        user_repository: UserRepository,
        history_repository: PredictionHistoryRepository,
        storage_service: StorageService,
        firebase_auth_service: FirebaseAuthService,
    ) -> None:
        self._user_repository = user_repository
        self._history_repository = history_repository
        self._storage_service = storage_service
        self._firebase_auth_service = firebase_auth_service

    def get_profile(self, user: CurrentUser) -> UserProfile:
        profile = self._user_repository.get_profile(user)
        return UserProfile(**profile)

    def update_profile(self, user: CurrentUser, display_name: str) -> UserProfile:
        profile = self._user_repository.update_profile(
            user=user,
            display_name=display_name,
        )
        return UserProfile(**profile)

    def delete_current_user_data(self, user: CurrentUser) -> DeleteResponse:
        self._ensure_recent_auth_for_account_delete(user)

        deleted_count = self._history_repository.delete_all(user)
        deleted_count += self._user_repository.delete_profile(user)
        self._storage_service.delete_user_prediction_images(user=user)
        auth_deleted = self._firebase_auth_service.delete_user_account(user=user)

        logger.info(
            "Current user account deletion completed. uid=%s deleted_count=%s auth_deleted=%s",
            user.uid,
            deleted_count,
            auth_deleted,
        )
        return DeleteResponse(
            deletedCount=deleted_count,
            authDeleted=auth_deleted,
        )

    def _ensure_recent_auth_for_account_delete(self, user: CurrentUser) -> None:
        settings = get_settings()
        if not settings.firebase_auth_required:
            return

        if user.auth_time is None:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Recent authentication is required.",
            )

        try:
            auth_age_seconds = max(0, int(time.time()) - int(user.auth_time))
        except (TypeError, ValueError) as exc:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Recent authentication is required.",
            ) from exc

        if auth_age_seconds > settings.account_delete_max_auth_age_seconds:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Recent authentication is required.",
            )
