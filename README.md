# FlorAI

FlorAI, kullanıcının kameradan çektiği veya galeriden seçtiği çiçek fotoğrafını
sınıflandıran mobil + backend tabanlı bir uygulamadır. Mobil uygulama Kotlin ve
Jetpack Compose ile geliştirilmiştir. Backend tarafında FastAPI kullanılır ve
eğitilmiş Ultralytics YOLO classification modeli üzerinden tahmin yapılır.

Proje; kullanıcı kimlik doğrulama, e-posta doğrulama, profil yönetimi, çiçek
tahmini, tahmin geçmişi, Firebase Storage üzerinde görsel saklama ve Firestore
üzerinden veri yönetimi gibi temel akışları içerir.

## Özellikler

- Firebase Authentication ile kayıt, giriş ve çıkış
- E-posta doğrulaması ve şifremi unuttum akışı
- Doğrulama tamamlandıktan sonra kullanıcı profil dokümanı oluşturma
- CameraX ile uygulama içinden fotoğraf çekme
- Photo Picker ile galeriden görsel seçme
- Backend üzerinden çiçek tahmini alma
- Desteklenen çiçek listesini backend üzerinden görüntüleme
- Tahmin geçmişini listeleme ve detaylarını inceleme
- Tahmin görsellerini Firebase Storage üzerinde saklama
- Tek tahmin kaydını veya tüm geçmişi silme
- Hesap silme işleminde kullanıcı verilerini ve Firebase Auth hesabını temizleme
- Düşük güvenli tahminlerde kullanıcıya açıklayıcı uyarı gösterme

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
- Hilt

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

## Genel Mimari

Mobil uygulama, kullanıcının seçtiği veya kamera ile çektiği görseli backend'e
gönderir. Backend görseli modelden geçirerek çiçek sınıfını tahmin eder ve
tahmin sonucunu mobil uygulamaya döndürür. Başarılı tahminlerde sonuç Firestore
üzerindeki tahmin geçmişine kaydedilir, görsel ise Firebase Storage altında
saklanır.

Kimlik doğrulama Firebase Authentication üzerinden yapılır. Mobil uygulama
backend isteklerinde Firebase ID token gönderir. Backend bu tokenı Firebase
Admin SDK ile doğrular ve kullanıcının yalnızca kendi verileri üzerinde işlem
yapmasını sağlar.

## Firebase Yapısı

Firestore koleksiyonları:

- `flowers/{flowerId}`: Çiçek bilgi dokümanları
- `users/{uid}`: Kullanıcı profil dokümanı
- `predictionHistory/{predictionId}`: Kullanıcı tahmin geçmişi

Storage path yapısı:

```text
prediction-images/{uid}/{predictionId}.jpg
```

Mobil uygulama Storage'a doğrudan yazmaz. Fotoğraf backend'e gönderilir, backend
Firebase Admin SDK ile görseli Storage'a yükler ve Firestore history kaydına
Storage nesne yolunu `imagePath` olarak yazar. Mobil uygulama bu yolu Firebase
Storage SDK ile yetkili şekilde okuyup görseli Coil ile gösterir.

`users/{uid}.predictionCount` alanı kullanıcının mevcut tahmin geçmişi kaydı
sayısını temsil eder. Geçmiş kaydı silindikçe bu değer backend tarafında senkron
tutulur.

## Model Güvenilirliği

Backend tahmin sonucunu yalnızca en yüksek skora göre değerlendirmez. En iyi
tahmin skoru belirlenen güven eşiğinin altındaysa veya en iyi sonuç ile ikinci
sonuç arasındaki fark düşükse yanıt `low_confidence` olarak işaretlenir.

Bu durumda mobil uygulama kullanıcıya modelin yeterince emin olmadığını, görselin
desteklenen çiçeklerden biri olmayabileceğini veya fotoğrafın daha net çekilmesi
gerektiğini açıklayan bir uyarı gösterir.

## Tahmin Geçmişi

Tahmin geçmişi, kullanıcı bazlı olarak Firestore üzerinde tutulur. Geçmiş listesi
sayfalama destekler ve her kayıt için detay ekranı bulunur. Detay ekranında
tahmin sonucu, kesinlik skoru, en yakın alternatifle skor farkı ve tüm sınıf
skorları görüntülenebilir.

Firestore tarafında tahmin geçmişi için `userId` ve `createdAt` alanlarını
kullanan composite index oluşturulmuştur. Böylece kayıtlar kullanıcıya göre
filtrelenip tarihe göre sıralı şekilde alınabilir.

## Güvenlik

Firestore kuralları, kullanıcının yalnızca kendi profilini ve kendi tahmin
geçmişini okuyabileceği şekilde düzenlenmiştir. Yazma ve silme işlemleri mobil
uygulamadan doğrudan yapılmaz; bu işlemler backend üzerinden Firebase Admin SDK
ile gerçekleştirilir.

Storage kuralları da tahmin görsellerinin yalnızca ilgili kullanıcı tarafından
okunmasına izin verir. Görsel yükleme ve silme işlemleri backend sorumluluğunda
tutulur.

## Testler

Projede backend ve mobil taraf için temel unit testler eklenmiştir. Backend
tarafında çiçek katalog eşleşmeleri, model güven değerlendirmesi ve hesap silme
servisi test edilir. Mobil tarafta auth doğrulama kuralları, profil ismi çözümleme
ve DTO-domain dönüşümleri kontrol edilir.

## Geliştirilebilir Alanlar

- Desteklenen çiçek sınıfı sayısı artırılabilir.
- Model daha geniş ve dengeli bir veri setiyle yeniden eğitilebilir.
- Desteklenmeyen çiçek veya çiçek olmayan görseller için daha gelişmiş kontrol
  mekanizması eklenebilir.
- Uygulama için daha kapsamlı UI testleri ve uçtan uca testler yazılabilir.
- Üretim ortamı için daha detaylı loglama ve hata izleme sistemi kurulabilir.
