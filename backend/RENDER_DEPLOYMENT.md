# Render Deployment

## Service

- Type: Web Service
- Runtime: Python 3
- Root Directory: `backend`
- Build Command: `pip install -r requirements.txt`
- Start Command: `uvicorn app.main:app --host 0.0.0.0 --port $PORT`

## Environment Variables

Set these values in the Render dashboard:

```text
APP_NAME=FlorAI Backend
MODEL_PATH=models/model.pt
PREDICTION_CONFIDENCE_THRESHOLD=0.60
PREDICTION_TOP_K=5
MAX_IMAGE_SIZE_MB=8
FIREBASE_AUTH_REQUIRED=true
REQUIRE_VERIFIED_EMAIL=true
FIRESTORE_ENABLED=true
FIREBASE_STORAGE_BUCKET=<your-firebase-storage-bucket>
PYTHON_VERSION=3.13.7
```

`FIREBASE_STORAGE_BUCKET` Firebase Storage bucket adidir. Firebase Console Storage
sayfasinda gorunen bucket degerini kullan; ornek formatlar:
`your-project-id.appspot.com` veya `your-project-id.firebasestorage.app`.

## Firebase Admin SDK

Recommended setup:

1. Add a Render Secret File named `firebase-admin.json`.
2. Paste the Firebase Admin SDK service account JSON into that secret file.
3. Add this environment variable:

```text
FIREBASE_SERVICE_ACCOUNT_PATH=/etc/secrets/firebase-admin.json
```

Alternative setup:

```text
FIREBASE_CREDENTIALS_JSON={"type":"service_account",...}
```

Do not commit Firebase Admin SDK JSON files to Git.

## Smoke Tests

After deploy:

```text
GET https://<service-name>.onrender.com/health
```

Expected:

```json
{
  "status": "ok",
  "modelLoaded": true,
  "classCount": 5,
  "firestoreEnabled": true,
  "storageEnabled": true
}
```

The `/predict` and `/prediction-history` endpoints require a Firebase ID token
when `FIREBASE_AUTH_REQUIRED=true`.
