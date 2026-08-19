# FlorAI Backend

FastAPI backend for classifying flower images with the trained Ultralytics model.
It also integrates with Firebase Auth, Firestore, and Firebase Storage for the
mobile app flow.

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
FIREBASE_STORAGE_BUCKET=your-project-id.firebasestorage.app
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
  "imageUrl": "https://firebasestorage.googleapis.com/...",
  "topPredictions": [],
  "source": "mobile",
  "createdAt": "server_timestamp"
}
```

The response `predictionId` is `null` while Firestore is disabled. It contains the
created history document ID when Firestore is enabled.

## Prediction History Query

The history endpoint supports a bounded limit:

```text
GET /prediction-history?limit=50
```

Use the detail endpoint to fetch a single authenticated user's history item:

```text
GET /prediction-history/{prediction_id}
```

The repository queries by `userId` and orders by `createdAt` descending when the
Firestore index is available. Recommended composite index:

```text
Collection: predictionHistory
Fields:
  userId Ascending
  createdAt Descending
```

## Firebase Storage

When `FIREBASE_STORAGE_BUCKET` is set, each uploaded prediction image is stored
under:

```text
prediction-images/{uid}/{predictionId}.{extension}
```

The generated download URL is written to `predictionHistory.imageUrl`. Deleting a
history item also attempts to remove its Storage image.

## Render Start Command

```text
uvicorn app.main:app --host 0.0.0.0 --port $PORT
```
