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
    options = {}
    if settings.firebase_storage_bucket:
        options["storageBucket"] = settings.firebase_storage_bucket

    if settings.firebase_credentials_json:
        return firebase_admin.initialize_app(
            credentials.Certificate(json.loads(settings.firebase_credentials_json)),
            options=options or None,
        )

    if settings.firebase_service_account_path:
        return firebase_admin.initialize_app(
            credentials.Certificate(settings.firebase_service_account_path),
            options=options or None,
        )

    return firebase_admin.initialize_app(options=options or None)


def get_firestore_client():
    initialize_firebase_app()

    from firebase_admin import firestore

    return firestore.client()


def get_storage_bucket():
    initialize_firebase_app()

    from firebase_admin import storage

    return storage.bucket()
