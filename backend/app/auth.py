from dataclasses import dataclass
from typing import Annotated

from fastapi import Header, HTTPException, status

from .config import get_settings
from .firebase_client import initialize_firebase_app


@dataclass(frozen=True)
class CurrentUser:
    uid: str
    email: str | None
    email_verified: bool
    display_name: str | None = None


async def get_current_user(
    authorization: Annotated[str | None, Header()] = None,
) -> CurrentUser:
    settings = get_settings()
    if not settings.firebase_auth_required:
        return CurrentUser(
            uid="local-dev",
            email=None,
            email_verified=True,
            display_name="Local Dev",
        )

    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing Firebase bearer token.",
        )

    token = authorization.removeprefix("Bearer ").strip()
    if not token:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing Firebase bearer token.",
        )

    try:
        initialize_firebase_app()
        from firebase_admin import auth

        decoded_token = auth.verify_id_token(token)
    except Exception as exc:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid Firebase token.",
        ) from exc

    email_verified = bool(decoded_token.get("email_verified", False))
    if settings.require_verified_email and not email_verified:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Email verification is required.",
        )

    return CurrentUser(
        uid=str(decoded_token["uid"]),
        email=decoded_token.get("email"),
        email_verified=email_verified,
        display_name=decoded_token.get("name"),
    )
