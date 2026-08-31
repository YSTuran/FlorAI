import os
import unittest

from fastapi import HTTPException

from app.auth import CurrentUser
from app.config import get_settings
from app.services.user_service import UserService


class FakeHistoryRepository:
    def __init__(self, deleted_count: int) -> None:
        self.deleted_count = deleted_count
        self.deleted_for_uid: str | None = None

    def delete_all(self, user: CurrentUser) -> int:
        self.deleted_for_uid = user.uid
        return self.deleted_count


class FakeUserRepository:
    def __init__(self, deleted_count: int) -> None:
        self.deleted_count = deleted_count
        self.deleted_for_uid: str | None = None

    def delete_profile(self, user: CurrentUser) -> int:
        self.deleted_for_uid = user.uid
        return self.deleted_count


class FakeStorageService:
    def __init__(self) -> None:
        self.deleted_for_uid: str | None = None

    def delete_user_prediction_images(self, *, user: CurrentUser) -> None:
        self.deleted_for_uid = user.uid


class FakeFirebaseAuthService:
    def __init__(self, auth_deleted: bool) -> None:
        self.auth_deleted = auth_deleted
        self.deleted_for_uid: str | None = None

    def delete_user_account(self, *, user: CurrentUser) -> bool:
        self.deleted_for_uid = user.uid
        return self.auth_deleted


class UserServiceTest(unittest.TestCase):
    def setUp(self):
        self._previous_firebase_auth_required = os.environ.get("FIREBASE_AUTH_REQUIRED")
        os.environ["FIREBASE_AUTH_REQUIRED"] = "false"
        get_settings.cache_clear()

    def tearDown(self):
        if self._previous_firebase_auth_required is None:
            os.environ.pop("FIREBASE_AUTH_REQUIRED", None)
        else:
            os.environ["FIREBASE_AUTH_REQUIRED"] = self._previous_firebase_auth_required
        get_settings.cache_clear()

    def test_delete_current_user_data_deletes_data_storage_and_auth(self):
        history_repository = FakeHistoryRepository(deleted_count=3)
        user_repository = FakeUserRepository(deleted_count=1)
        storage_service = FakeStorageService()
        firebase_auth_service = FakeFirebaseAuthService(auth_deleted=True)
        service = UserService(
            user_repository=user_repository,
            history_repository=history_repository,
            storage_service=storage_service,
            firebase_auth_service=firebase_auth_service,
        )
        user = CurrentUser(
            uid="uid-1",
            email="user@example.com",
            email_verified=True,
            auth_time=None,
        )

        result = service.delete_current_user_data(user)

        self.assertEqual(4, result.deletedCount)
        self.assertTrue(result.authDeleted)
        self.assertEqual("uid-1", history_repository.deleted_for_uid)
        self.assertEqual("uid-1", user_repository.deleted_for_uid)
        self.assertEqual("uid-1", storage_service.deleted_for_uid)
        self.assertEqual("uid-1", firebase_auth_service.deleted_for_uid)

    def test_delete_current_user_data_reports_when_auth_is_not_deleted(self):
        service = UserService(
            user_repository=FakeUserRepository(deleted_count=0),
            history_repository=FakeHistoryRepository(deleted_count=0),
            storage_service=FakeStorageService(),
            firebase_auth_service=FakeFirebaseAuthService(auth_deleted=False),
        )
        user = CurrentUser(
            uid="local-dev",
            email=None,
            email_verified=True,
            auth_time=None,
        )

        result = service.delete_current_user_data(user)

        self.assertEqual(0, result.deletedCount)
        self.assertFalse(result.authDeleted)

    def test_delete_current_user_data_requires_recent_auth_when_auth_is_enabled(self):
        os.environ["FIREBASE_AUTH_REQUIRED"] = "true"
        os.environ["ACCOUNT_DELETE_MAX_AUTH_AGE_SECONDS"] = "300"
        get_settings.cache_clear()
        service = UserService(
            user_repository=FakeUserRepository(deleted_count=0),
            history_repository=FakeHistoryRepository(deleted_count=0),
            storage_service=FakeStorageService(),
            firebase_auth_service=FakeFirebaseAuthService(auth_deleted=True),
        )
        user = CurrentUser(
            uid="uid-1",
            email="user@example.com",
            email_verified=True,
            auth_time=1,
        )

        with self.assertRaises(HTTPException) as context:
            service.delete_current_user_data(user)

        self.assertEqual(401, context.exception.status_code)
        self.assertEqual("Recent authentication is required.", context.exception.detail)


if __name__ == "__main__":
    unittest.main()
