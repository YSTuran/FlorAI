from .auth import CurrentUser
from .repositories.common import DEFAULT_USER_ROLE, MAX_HISTORY_ITEMS
from .repositories.flower_repository import FlowerRepository
from .repositories.prediction_history_repository import PredictionHistoryRepository
from .repositories.user_repository import UserRepository
from .schemas import PredictionItem


class FirestoreRepository:
    """Backward-compatible facade for the split Firestore repositories."""

    def __init__(self) -> None:
        self.flowers = FlowerRepository()
        self.users = UserRepository()
        self.history = PredictionHistoryRepository()

    @property
    def is_enabled(self) -> bool:
        return self.history.is_enabled

    def get_flower(self, flower_id: str | None):
        return self.flowers.get_flower(flower_id)

    def get_user_profile(self, user: CurrentUser) -> dict:
        return self.users.get_profile(user)

    def update_user_profile(self, user: CurrentUser, display_name: str) -> dict:
        return self.users.update_profile(user=user, display_name=display_name)

    def record_prediction_for_user(self, user: CurrentUser) -> None:
        self.users.record_prediction(user)

    def create_prediction_history(
        self,
        user: CurrentUser,
        best_prediction: PredictionItem,
        top_predictions: list[PredictionItem],
        low_confidence: bool,
        confidence_gap: float | None = None,
        confidence_note: str | None = None,
        prediction_id: str | None = None,
        image_path: str | None = None,
    ) -> str | None:
        return self.history.create(
            user=user,
            best_prediction=best_prediction,
            top_predictions=top_predictions,
            low_confidence=low_confidence,
            confidence_gap=confidence_gap,
            confidence_note=confidence_note,
            prediction_id=prediction_id,
            image_path=image_path,
        )

    def create_prediction_history_id(self) -> str | None:
        return self.history.create_id()

    def list_prediction_history(
        self,
        user: CurrentUser,
        limit: int = MAX_HISTORY_ITEMS,
    ) -> list[dict]:
        return self.history.list(user=user, limit=limit)["items"]

    def get_prediction_history_item(
        self,
        user: CurrentUser,
        prediction_id: str,
    ) -> dict:
        return self.history.get(user=user, prediction_id=prediction_id)

    def delete_prediction_history_item(self, user: CurrentUser, prediction_id: str) -> int:
        return self.history.delete_item(user=user, prediction_id=prediction_id)

    def delete_prediction_history(self, user: CurrentUser) -> int:
        return self.history.delete_all(user)

    def delete_user_data(self, user: CurrentUser) -> int:
        deleted_count = self.history.delete_all(user)
        deleted_count += self.users.delete_profile(user)
        return deleted_count


__all__ = ["DEFAULT_USER_ROLE", "MAX_HISTORY_ITEMS", "FirestoreRepository"]
