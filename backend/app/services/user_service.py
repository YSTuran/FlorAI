from ..auth import CurrentUser
from ..repositories.prediction_history_repository import PredictionHistoryRepository
from ..repositories.user_repository import UserRepository
from ..schemas import DeleteResponse, UserProfile
from ..storage_service import StorageService


class UserService:
    def __init__(
        self,
        *,
        user_repository: UserRepository,
        history_repository: PredictionHistoryRepository,
        storage_service: StorageService,
    ) -> None:
        self._user_repository = user_repository
        self._history_repository = history_repository
        self._storage_service = storage_service

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
        deleted_count = self._history_repository.delete_all(user)
        deleted_count += self._user_repository.delete_profile(user)
        self._storage_service.delete_user_prediction_images(user=user)
        return DeleteResponse(deletedCount=deleted_count)
