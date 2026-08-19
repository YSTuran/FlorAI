# FlorAI

FlorAI, kullanicinin kameradan cektigi veya galeriden sectigi cicek fotografini
siniflandiran mobil + backend tabanli bir uygulamadir. Backend FastAPI ile
calisir, egitilmis Ultralytics YOLO classification modelini kullanir ve Firebase
Auth, Firestore, Storage servisleriyle entegre olur.

## Ozellikler

- Firebase Authentication ile kayit, giris, cikis
- E-posta dogrulamasi ve sifremi unuttum akisi
- Dogrulama tamamlandiktan sonra `users/{uid}` profil dokumani olusturma
- CameraX ile uygulama icinden fotograf cekme
- Photo Picker ile galeriden gorsel secme
- Backend uzerinden cicek tahmini alma
- Tahmin gecmisini Firestore uzerinden listeleme
- Tahmin gorsellerini Firebase Storage'da saklama
- Tek tahmin veya tum gecmisi silme

## Teknolojiler

Mobil:

- Kotlin
- Jetpack Compose
- MVVM
- Navigation Compose
- CameraX
- Retrofit
- Coroutines
- Firebase Auth

Backend:

- Python
- FastAPI
- Ultralytics YOLO Classification
- Firebase Admin SDK
- Firestore
- Firebase Storage

Dagitim:

- Render Python Web Service
- Otomatik HTTPS

## Proje Yapisi

```text
FlorAI/
  Android/          Android mobil uygulama
  backend/          FastAPI backend
  firestore.rules   Firestore guvenlik kurallari
  storage.rules     Firebase Storage guvenlik kurallari
  firebase.json     Firebase rules deploy ayarlari
```

## Backend Calistirma

```powershell
cd backend
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload
```

Temel kontrol:

```text
GET http://127.0.0.1:8000/health
```

## Backend Ortam Degiskenleri

```text
APP_NAME=FlorAI Backend
MODEL_PATH=models/model.pt
PREDICTION_CONFIDENCE_THRESHOLD=0.60
PREDICTION_TOP_K=5
MAX_IMAGE_SIZE_MB=8
FIREBASE_AUTH_REQUIRED=true
REQUIRE_VERIFIED_EMAIL=true
FIRESTORE_ENABLED=true
FIREBASE_STORAGE_BUCKET=your-project-id.firebasestorage.app
FIREBASE_SERVICE_ACCOUNT_PATH=secrets/firebase-admin.json
```

Render tarafinda `FIREBASE_SERVICE_ACCOUNT_PATH` genelde secret file ile
`/etc/secrets/firebase-admin.json` olarak tanimlanir. Firebase Admin SDK dosyasi
repo'ya eklenmemelidir.

## Firebase Yapisi

Firestore koleksiyonlari:

- `flowers/{flowerId}`: Cicek bilgi dokumanlari
- `users/{uid}`: Kullanici profil dokumani
- `predictionHistory/{predictionId}`: Kullanici tahmin gecmisi

Storage path yapisi:

```text
prediction-images/{uid}/{predictionId}.jpg
```

Mobil uygulama Storage'a dogrudan yazmaz. Fotograf backend'e gider, backend
Firebase Admin SDK ile Storage'a yukler ve olusan URL'i Firestore history
kaydina yazar.

## History Sorgusu

Tahmin gecmisi endpointi:

```text
GET /prediction-history?limit=50
```

Backend, kayitlari `userId` filtresi ve `createdAt DESC` siralamasi ile almaya
calisir. En iyi performans icin Firestore'da su composite index onerilir:

```text
Collection: predictionHistory
Fields:
  userId Ascending
  createdAt Descending
```

Index henuz hazir degilse backend kucuk olcekli fallback ile kayitlari alip
uygulama tarafinda siralar.

## Firebase Rules Deploy

```powershell
firebase deploy --only firestore,storage
```

## Android Calistirma

Local backend ile:

```powershell
cd Android
.\gradlew.bat :app:assembleLocalDebug
```

Render backend ile:

```powershell
cd Android
.\gradlew.bat :app:assembleRenderDebug
```

Build flavor API adresleri `Android/app/build.gradle.kts` icinde tutulur.

## Render

Render servis ayarlari:

```text
Root Directory: backend
Build Command: pip install -r requirements.txt
Start Command: uvicorn app.main:app --host 0.0.0.0 --port $PORT
```

Deploy sonrasi kontrol:

```text
GET https://florai-jd3v.onrender.com/health
```

Beklenen alanlar:

```json
{
  "modelLoaded": true,
  "firestoreEnabled": true,
  "storageEnabled": true
}
```
