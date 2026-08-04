# FlorAI Backend

FastAPI backend for classifying flower images with the trained Ultralytics model.

## Current Model Classes

- `daisy` -> Papatya
- `dandelion` -> Karahindiba
- `roses` -> Gul
- `sunflowers` -> Aycicegi
- `tulips` -> Lale

## Local Run

```powershell
cd backend
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload
```

Open:

- `GET http://127.0.0.1:8000/health`
- `POST http://127.0.0.1:8000/predict` with form-data field `image`

## Firebase Auth

Local development defaults to `FIREBASE_AUTH_REQUIRED=false`.

For production:

```text
FIREBASE_AUTH_REQUIRED=true
REQUIRE_VERIFIED_EMAIL=true
FIREBASE_CREDENTIALS_JSON={...}
```

The mobile app should send the Firebase ID token in the request header:

```text
Authorization: Bearer <firebase_id_token>
```

## Render Start Command

```text
uvicorn app.main:app --host 0.0.0.0 --port $PORT
```
