# FlorAI Backend

FastAPI backend for classifying flower images with the trained Ultralytics model.
It also integrates with Firebase Auth, Firestore, and Firebase Storage for the
mobile app flow.

## Current Model Classes

- `daisy` -> Papatya
- `dandelion` -> Karahindiba
- `roses` -> Gül
- `sunflowers` -> Ayçiçeği
- `tulips` -> Lale

## Flower Catalog Endpoint

The mobile app can fetch the supported flower catalog from the backend:

```text
GET /flowers
```

When Firestore is enabled, the backend reads `flowers/{flowerId}` documents. If a
document is missing, the local catalog matching the trained model classes is used
as a fallback.

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
    "confidenceGap": 0.79,
    "confidenceNote": null,
    "height": "10-20 cm",
    "habitats": ["Çayırlar", "yol kenarları"],
    "bloomMonths": ["Mart", "Nisan", "Mayıs"],
    "details": "Papatya, ılıman iklimlerde yaygın görülen bir çiçektir.",
    "extraFacts": ["Güneşli veya yarı gölgeli alanlarda iyi gelişir."]
  }
}
```

## Confidence Handling

The backend marks a prediction as `low_confidence` when the best score is below
`PREDICTION_CONFIDENCE_THRESHOLD` or when the best result is too close to the
second result according to `PREDICTION_CONFIDENCE_MARGIN_THRESHOLD`. In these
cases the response includes `confidenceNote`, which the mobile app can show as a
user-facing warning.

## Firebase Auth

Local development defaults to `FIREBASE_AUTH_REQUIRED=false`.

For production:

```text
FIREBASE_AUTH_REQUIRED=true
REQUIRE_VERIFIED_EMAIL=true
FIRESTORE_ENABLED=true
FIREBASE_STORAGE_BUCKET=your-project-id.firebasestorage.app
ACCOUNT_DELETE_MAX_AUTH_AGE_SECONDS=300
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
  "confidenceGap": 0.79,
  "confidenceNote": null,
  "imagePath": "prediction-images/firebase_user_uid/prediction_id.jpg",
  "topPredictions": [],
  "source": "mobile",
  "createdAt": "server_timestamp"
}
```

`users/{uid}.predictionCount` represents the current number of prediction history
records. Deleting one or all history records updates this value.

The response `predictionId` is `null` while Firestore is disabled. It contains the
created history document ID when Firestore is enabled.

## Prediction History Query

The history endpoint supports a bounded limit:

```text
GET /prediction-history?limit=20
GET /prediction-history?limit=20&cursor=<nextCursor>
```

The response includes `nextCursor` when another page is available.

Use the detail endpoint to fetch a single authenticated user's history item:

```text
GET /prediction-history/{prediction_id}
```

The repository queries by `userId` and orders by `createdAt` descending when the
Firestore index is available. Production expects this composite index:

```text
Collection: predictionHistory
Fields:
  userId Ascending
  createdAt Descending
```

If this index is missing, the backend returns a clear service error instead of
streaming and sorting all user history documents in memory.

## Account Deletion

`DELETE /users/me` deletes the current user's prediction history, profile
document, Storage images, and Firebase Auth account. In production this endpoint
requires a recently refreshed Firebase ID token. The mobile app should
reauthenticate the user with their password before calling it.

## Tests

```powershell
cd backend
python -m unittest discover -s tests
```

## Firebase Storage

When `FIREBASE_STORAGE_BUCKET` is set, each uploaded prediction image is stored
under:

```text
prediction-images/{uid}/{predictionId}.{extension}
```

The Storage object path is written to `predictionHistory.imagePath`. The mobile
app resolves that path through Firebase Storage SDK and loads the image with
Coil. Older `predictionHistory.imageUrl` values are still supported for backward
compatibility. Deleting a history item also attempts to remove its Storage image.

## Render Start Command

```text
uvicorn app.main:app --host 0.0.0.0 --port $PORT
```
