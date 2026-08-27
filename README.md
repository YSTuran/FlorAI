# FlorAI

FlorAI, kullanıcının kameradan çektiği veya galeriden seçtiği çiçek fotoğrafını
sınıflandıran mobil + backend tabanlı bir uygulamadır. Backend FastAPI ile
çalışır, eğitilmiş Ultralytics YOLO classification modelini kullanır ve Firebase
Auth, Firestore, Storage servisleriyle entegre olur.

## Özellikler

- Firebase Authentication ile kayıt, giriş, çıkış
- E-posta doğrulaması ve şifremi unuttum akışı
- Doğrulama tamamlandıktan sonra `users/{uid}` profil dokümanı oluşturma
- CameraX ile uygulama içinden fotoğraf çekme
- Photo Picker ile galeriden görsel seçme
- Backend üzerinden çiçek tahmini alma
- Desteklenen çiçek listesini backend üzerinden alma
- Tahmin geçmişini Firestore üzerinden listeleme
- Tahmin görsellerini Firebase Storage'da saklama
- Tek tahmin veya tüm geçmişi silme

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
- Firebase Storage SDK
- Coil

Backend:

- Python
- FastAPI
- Ultralytics YOLO Classification
- Firebase Admin SDK
- Firestore
- Firebase Storage

Dağıtım:

- Render Python Web Service
- Otomatik HTTPS

## Proje Yapısı

```text
FlorAI/
  Android/          Android mobil uygulama
  backend/          FastAPI backend
  firestore.rules   Firestore güvenlik kuralları
  storage.rules     Firebase Storage güvenlik kuralları
  firebase.json     Firebase rules deploy ayarları
```

## Backend Çalıştırma

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

## Backend Ortam Değişkenleri

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

Render tarafında `FIREBASE_SERVICE_ACCOUNT_PATH` genelde secret file ile
`/etc/secrets/firebase-admin.json` olarak tanımlanır. Firebase Admin SDK dosyası
repo'ya eklenmemelidir.

## Firebase Yapısı

Firestore koleksiyonları:

- `flowers/{flowerId}`: Çiçek bilgi dokümanları
- `users/{uid}`: Kullanıcı profil dokümanı
- `predictionHistory/{predictionId}`: Kullanıcı tahmin geçmişi

Storage path yapisi:

```text
prediction-images/{uid}/{predictionId}.jpg
```

Mobil uygulama Storage'a doğrudan yazmaz. Fotoğraf backend'e gider, backend
Firebase Admin SDK ile Storage'a yükler ve Firestore history kaydına Storage
nesne yolunu `imagePath` olarak yazar. Mobil uygulama bu yolu Firebase Storage
SDK ile yetkili şekilde okuyup görseli Coil ile yükler. Eski kayıtlarda
bulunabilecek `imageUrl` alanı yalnızca geriye dönük uyumluluk için kullanılır.

`users/{uid}.predictionCount` alanı toplam tahmin sayısından ziyade kullanıcının
mevcut tahmin geçmişi kaydı sayısını temsil eder. Geçmiş kaydı silindikçe bu
değer backend tarafında senkron tutulur.

## Çiçek Listesi

Mobil uygulama desteklenen çiçek listesini backend'den alır:

```text
GET /flowers
```

Backend, Firestore'daki `flowers/{flowerId}` dokümanlarını kullanır. Eksik
doküman olursa eğitim sınıflarıyla uyumlu yerel katalog yedek olarak kullanılır.

## History Sorgusu

Tahmin gecmisi endpointi:

```text
GET /prediction-history?limit=20
GET /prediction-history?limit=20&cursor=<nextCursor>
```

Yanıtta `items` ile birlikte sonraki sayfa varsa `nextCursor` döner.

Tekil tahmin gecmisi detayi:

```text
GET /prediction-history/{prediction_id}
```

Backend, kayıtları `userId` filtresi ve `createdAt DESC` sıralaması ile almaya
çalışır. En iyi performans için Firestore'da şu composite index önerilir:

```text
Collection: predictionHistory
Fields:
  userId Ascending
  createdAt Descending
```

Index henüz hazır değilse backend küçük ölçekli fallback ile kayıtları alıp
uygulama tarafında sıralar.

## Firebase Rules Deploy

```powershell
firebase deploy --only firestore,storage
```

## Android Çalıştırma

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

Build flavor API adresleri `Android/app/build.gradle.kts` içinde tutulur.

## Testler

Backend unit testleri:

```powershell
cd backend
python -m unittest discover -s tests
```

Android unit testleri:

```powershell
cd Android
.\gradlew.bat :app:testLocalDebugUnitTest
```

## Render

Render servis ayarlari:

```text
Root Directory: backend
Build Command: pip install -r requirements.txt
Start Command: uvicorn app.main:app --host 0.0.0.0 --port $PORT
```

Deploy sonrası kontrol:

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
