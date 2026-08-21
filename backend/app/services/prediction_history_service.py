from ..auth import CurrentUser
from ..repositories.prediction_history_repository import PredictionHistoryRepository
from ..schemas import DeleteResponse, PredictionHistoryItem, PredictionHistoryResponse
from ..storage_service import StorageService


class PredictionHistoryService:
    def __init__(
        self,
        *,
        history_repository: PredictionHistoryRepository,
        storage_service: StorageService,
    ) -> None:
        self._history_repository = history_repository
        self._storage_service = storage_service

    def list_history(
        self,
        *,
        user: CurrentUser,
        limit: int,
    ) -> PredictionHistoryResponse:
        items = self._history_repository.list(user=user, limit=limit)
        return PredictionHistoryResponse(
            items=[PredictionHistoryItem(**item) for item in items]
        )

    def get_history_item(
        self,
        *,
        user: CurrentUser,
        prediction_id: str,
    ) -> PredictionHistoryItem:
        item = self._history_repository.get(
            user=user,
            prediction_id=prediction_id,
        )
        return PredictionHistoryItem(**item)

    def delete_history_item(
        self,
        *,
        user: CurrentUser,
        prediction_id: str,
    ) -> DeleteResponse:
        deleted_count = self._history_repository.delete_item(
            user=user,
            prediction_id=prediction_id,
        )
        if deleted_count:
            self._storage_service.delete_prediction_image(
                user=user,
                prediction_id=prediction_id,
            )
        return DeleteResponse(deletedCount=deleted_count)

    def delete_history(self, user: CurrentUser) -> DeleteResponse:
        deleted_count = self._history_repository.delete_all(user)
        if deleted_count:
            self._storage_service.delete_user_prediction_images(user=user)
        return DeleteResponse(deletedCount=deleted_count)
