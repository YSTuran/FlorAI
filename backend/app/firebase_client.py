import json

from .config import get_settings


def initialize_firebase_app():
    try:
        import firebase_admin
        from firebase_admin import credentials
    except ImportError as exc:
        raise RuntimeError("firebase-admin is required for Firebase features.") from exc

    if firebase_admin._apps:
        return firebase_admin.get_app()

    settings = get_settings()
    if settings.firebase_credentials_json:
        return firebase_admin.initialize_app(
            credentials.Certificate(json.loads(settings.firebase_credentials_json))
        )

    if settings.firebase_service_account_path:
        return firebase_admin.initialize_app(
            credentials.Certificate(settings.firebase_service_account_path)
        )

    return firebase_admin.initialize_app()


def get_firestore_client():
    initialize_firebase_app()

    from firebase_admin import firestore

    return firestore.client()
