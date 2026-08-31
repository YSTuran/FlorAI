import logging

from fastapi import HTTPException, status

from ..auth import CurrentUser
from ..config import get_settings
from ..firebase_client import initialize_firebase_app


logger = logging.getLogger(__name__)


class FirebaseAuthService:
    def delete_user_account(self, *, user: CurrentUser) -> bool:
        if not get_settings().firebase_auth_required:
            return False

        try:
            initialize_firebase_app()
            from firebase_admin import auth
        except Exception as exc:
            logger.exception("Firebase Auth service could not be initialized.")
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Firebase Auth account could not be deleted.",
            ) from exc

        try:
            auth.delete_user(user.uid)
            logger.info("Firebase Auth user deleted. uid=%s", user.uid)
            return True
        except auth.UserNotFoundError:
            logger.info("Firebase Auth user was already deleted. uid=%s", user.uid)
            return True
        except Exception as exc:
            logger.exception("Firebase Auth user could not be deleted. uid=%s", user.uid)
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Firebase Auth account could not be deleted.",
            ) from exc
