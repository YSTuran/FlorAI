from ..auth import CurrentUser
from ..repositories.prediction_history_repository import PredictionHistoryRepository
from ..repositories.user_repository import UserRepository
from ..schemas import DeleteResponse, PredictionHistoryItem, PredictionHistoryResponse
from ..storage_service import StorageService


class PredictionHistoryService:
    def __init__(
        self,
        *,
        history_repository: PredictionHistoryRepository,
        user_repository: UserRepository,
        storage_service: StorageService,
    ) -> None:
        self._history_repository = history_repository
        self._user_repository = user_repository
        self._storage_service = storage_service

    def list_history(
        self,
        *,
        user: CurrentUser,
        limit: int,
        cursor: str | None,
    ) -> PredictionHistoryResponse:
        page = self._history_repository.list(
            user=user,
            limit=limit,
            cursor=cursor,
        )
        return PredictionHistoryResponse(
            items=[PredictionHistoryItem(**item) for item in page["items"]],
            nextCursor=page["nextCursor"],
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
            self._user_repository.set_prediction_count(
                user=user,
                prediction_count=self._history_repository.count(user),
            )
        return DeleteResponse(deletedCount=deleted_count)

    def delete_history(self, user: CurrentUser) -> DeleteResponse:
        deleted_count = self._history_repository.delete_all(user)
        if deleted_count:
            self._storage_service.delete_user_prediction_images(user=user)
            self._user_repository.set_prediction_count(
                user=user,
                prediction_count=0,
            )
        return DeleteResponse(deletedCount=deleted_count)
