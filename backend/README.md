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

Example predict response:

```json
{
  "status": "success",
  "predictionId": null,
  "result": {
    "flowerId": "papatya",
    "classId": 0,
    "modelLabel": "daisy",
    "name": "Papatya",
    "scientificName": "Bellis perennis",
    "confidence": 0.91,
    "lowConfidence": false,
    "height": "10-20 cm",
    "habitats": ["Cayirlar", "yol kenarlari"],
    "bloomMonths": ["Mart", "Nisan", "Mayis"],
    "details": "Papatya, iliman iklimlerde yaygin gorulen bir cicektir.",
    "extraFacts": ["Gunesli veya yari golgeli alanlarda iyi gelisir."]
  }
}
```

## Firebase Auth

Local development defaults to `FIREBASE_AUTH_REQUIRED=false`.

For production:

```text
FIREBASE_AUTH_REQUIRED=true
REQUIRE_VERIFIED_EMAIL=true
FIRESTORE_ENABLED=true
FIREBASE_CREDENTIALS_JSON={...}
```

The mobile app should send the Firebase ID token in the request header:

```text
Authorization: Bearer <firebase_id_token>
```

## Firestore

When `FIRESTORE_ENABLED=true`, the backend reads flower details from:

```text
flowers/{flowerId}
```

The expected flower document IDs are:

```text
papatya
karahindiba
gul
aycicegi
lale
```

Each successful `/predict` request also creates a history document:

```text
predictionHistory/{predictionId}
```

Stored fields include:

```json
{
  "userId": "firebase_user_uid",
  "predictedFlowerId": "papatya",
  "displayName": "Papatya",
  "modelLabel": "daisy",
  "classId": 0,
  "confidence": 0.91,
  "lowConfidence": false,
  "topPredictions": [],
  "source": "mobile",
  "createdAt": "server_timestamp"
}
```

The response `predictionId` is `null` while Firestore is disabled. It contains the
created history document ID when Firestore is enabled.

## Render Start Command

```text
uvicorn app.main:app --host 0.0.0.0 --port $PORT
```
