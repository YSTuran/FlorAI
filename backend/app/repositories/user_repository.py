from fastapi import HTTPException, status

from ..auth import CurrentUser
from .common import (
    DEFAULT_USER_ROLE,
    FirestoreRepositoryBase,
    clean_display_name,
    timestamp_to_iso,
)


class UserRepository(FirestoreRepositoryBase):
    def get_profile(self, user: CurrentUser) -> dict:
        if not self.is_enabled:
            return self._local_profile_payload(user)

        client = self._client()
        from firebase_admin import firestore

        doc_ref = client.collection("users").document(user.uid)
        try:
            doc = doc_ref.get()
            if not doc.exists:
                doc_ref.set(
                    {
                        "uid": user.uid,
                        "email": user.email,
                        "displayName": clean_display_name(user.display_name),
                        "role": DEFAULT_USER_ROLE,
                        "predictionCount": 0,
                        "createdAt": firestore.SERVER_TIMESTAMP,
                        "updatedAt": firestore.SERVER_TIMESTAMP,
                        "lastActiveAt": firestore.SERVER_TIMESTAMP,
                    }
                )
                doc = doc_ref.get()
            else:
                data = doc.to_dict() or {}
                update_data = {
                    "email": user.email,
                    "lastActiveAt": firestore.SERVER_TIMESTAMP,
                }
                if not data.get("displayName") and user.display_name:
                    update_data["displayName"] = clean_display_name(user.display_name)
                doc_ref.set(update_data, merge=True)
                doc = doc_ref.get()
        except HTTPException:
            raise
        except Exception as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="User profile is not available.",
            ) from exc

        return self._profile_payload_from_data(
            uid=user.uid,
            user=user,
            data=doc.to_dict() or {},
        )

    def update_profile(self, user: CurrentUser, display_name: str) -> dict:
        cleaned_display_name = clean_display_name(display_name)
        if not cleaned_display_name:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Display name is required.",
            )

        if not self.is_enabled:
            return self._local_profile_payload(user, cleaned_display_name)

        client = self._client()
        from firebase_admin import firestore

        doc_ref = client.collection("users").document(user.uid)
        try:
            doc = doc_ref.get()
            if not doc.exists:
                doc_ref.set(
                    {
                        "uid": user.uid,
                        "email": user.email,
                        "displayName": cleaned_display_name,
                        "role": DEFAULT_USER_ROLE,
                        "predictionCount": 0,
                        "createdAt": firestore.SERVER_TIMESTAMP,
                        "updatedAt": firestore.SERVER_TIMESTAMP,
                        "lastActiveAt": firestore.SERVER_TIMESTAMP,
                    }
                )
            else:
                doc_ref.set(
                    {
                        "email": user.email,
                        "displayName": cleaned_display_name,
                        "updatedAt": firestore.SERVER_TIMESTAMP,
                        "lastActiveAt": firestore.SERVER_TIMESTAMP,
                    },
                    merge=True,
                )
            doc = doc_ref.get()
        except HTTPException:
            raise
        except Exception as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="User profile is not available.",
            ) from exc

        return self._profile_payload_from_data(
            uid=user.uid,
            user=user,
            data=doc.to_dict() or {},
        )

    def record_prediction(self, user: CurrentUser) -> None:
        if not self.is_enabled:
            return

        client = self._client()
        from firebase_admin import firestore

        doc_ref = client.collection("users").document(user.uid)
        try:
            doc = doc_ref.get()
            if not doc.exists:
                doc_ref.set(
                    {
                        "uid": user.uid,
                        "email": user.email,
                        "displayName": clean_display_name(user.display_name),
                        "role": DEFAULT_USER_ROLE,
                        "predictionCount": 1,
                        "createdAt": firestore.SERVER_TIMESTAMP,
                        "updatedAt": firestore.SERVER_TIMESTAMP,
                        "lastActiveAt": firestore.SERVER_TIMESTAMP,
                    }
                )
                return

            doc_ref.set(
                {
                    "email": user.email,
                    "predictionCount": firestore.Increment(1),
                    "updatedAt": firestore.SERVER_TIMESTAMP,
                    "lastActiveAt": firestore.SERVER_TIMESTAMP,
                },
                merge=True,
            )
        except Exception:
            return

    def delete_profile(self, user: CurrentUser) -> int:
        if not self.is_enabled:
            return 0

        try:
            doc_ref = self._client().collection("users").document(user.uid)
            doc = doc_ref.get()
            if not doc.exists:
                return 0

            doc_ref.delete()
        except HTTPException:
            raise
        except Exception as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="User data could not be deleted.",
            ) from exc

        return 1

    def _profile_payload_from_data(
        self,
        *,
        uid: str,
        user: CurrentUser,
        data: dict,
    ) -> dict:
        return {
            "uid": data.get("uid") or uid,
            "email": data.get("email") or user.email,
            "displayName": clean_display_name(data.get("displayName")),
            "role": data.get("role") or DEFAULT_USER_ROLE,
            "predictionCount": int(data.get("predictionCount") or 0),
            "createdAt": timestamp_to_iso(data.get("createdAt")),
            "updatedAt": timestamp_to_iso(data.get("updatedAt")),
            "lastActiveAt": timestamp_to_iso(data.get("lastActiveAt")),
        }

    def _local_profile_payload(
        self,
        user: CurrentUser,
        display_name: str | None = None,
    ) -> dict:
        return {
            "uid": user.uid,
            "email": user.email,
            "displayName": clean_display_name(display_name or user.display_name),
            "role": DEFAULT_USER_ROLE,
            "predictionCount": 0,
            "createdAt": None,
            "updatedAt": None,
            "lastActiveAt": None,
        }
