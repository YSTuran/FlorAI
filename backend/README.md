# FlorAI Backend

FlorAI backend, mobil uygulamadan gelen çiçek görsellerini sınıflandırmak için
FastAPI ile geliştirilmiş bir servistir. Eğitilmiş Ultralytics YOLO
classification modelini kullanır ve Firebase Auth, Firestore ile Firebase
Storage servisleriyle entegre çalışır.

Backend'in temel sorumluluğu mobil uygulamadan gelen istekleri doğrulamak,
görseli modele göndermek, tahmin sonucunu üretmek, ilgili kullanıcıya ait tahmin
geçmişini Firestore'a kaydetmek ve tahmin görselini Firebase Storage üzerinde
saklamaktır.

## Model Sınıfları

Eğitilen modelde bulunan sınıflar ve uygulamadaki karşılıkları:

- `daisy` -> Papatya
- `dandelion` -> Karahindiba
- `roses` -> Gül
- `sunflowers` -> Ayçiçeği
- `tulips` -> Lale

## API Yapısı

Backend aşağıdaki temel işlevleri sağlar:

- Uygulama ve sağlık bilgisi döndürme
- Desteklenen çiçek listesini sunma
- Görsel üzerinden çiçek tahmini yapma
- Kullanıcının tahmin geçmişini listeleme
- Tekil tahmin geçmişi detayını döndürme
- Tahmin geçmişi kaydı silme
- Kullanıcı profilini görüntüleme ve güncelleme
- Kullanıcı hesabını ve ilişkili verileri silme

## Tahmin Yanıtı

Tahmin sonucunda backend, kullanıcıya gösterilecek ana sonucu ve modelin güven
durumunu içeren bir yanıt üretir.

Örnek tahmin yanıtı:

```json
{
  "status": "success",
  "predictionId": "prediction_id",
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

## Model Güven Değerlendirmesi

Backend tahmin sonucunu yalnızca en yüksek skora göre değerlendirmez. En iyi
tahmin skoru belirlenen güven eşiğinin altındaysa veya en iyi sonuç ile ikinci
sonuç arasındaki fark yeterince yüksek değilse tahmin `low_confidence` olarak
işaretlenir.

Bu durumda yanıt içinde `confidenceNote` alanı gönderilir. Mobil uygulama bu
alanı kullanarak kullanıcıya modelin yeterince emin olmadığını, görselin
desteklenen çiçeklerden biri olmayabileceğini veya daha net bir fotoğrafla tekrar
denenmesi gerektiğini açıklar.

## Firebase Auth

Backend, mobil uygulamadan gelen Firebase ID token bilgisini doğrular. Böylece
kullanıcının yalnızca kendi profiline, kendi tahmin geçmişine ve kendi görsel
kayıtlarına erişmesi sağlanır.

E-posta doğrulaması aktif olduğunda doğrulanmamış kullanıcıların tahmin ve geçmiş
gibi korumalı endpointleri kullanmasına izin verilmez.

## Firestore

Backend Firestore üzerinde üç temel koleksiyonla çalışır:

```text
flowers/{flowerId}
users/{uid}
predictionHistory/{predictionId}
```

`flowers` koleksiyonu çiçek bilgilerini tutar. `users` koleksiyonu kullanıcı
profil bilgilerini ve mevcut tahmin geçmişi sayısını içerir. `predictionHistory`
koleksiyonu ise kullanıcının yaptığı tahminleri, tahmin skorlarını, Storage
görsel yolunu ve oluşturulma tarihini saklar.

Tahmin geçmişi kayıtları kullanıcıya göre filtrelenir ve oluşturulma tarihine
göre yeniden eskiye sıralanır. Bu sorgu için Firestore üzerinde `userId` ve
`createdAt` alanlarını kullanan composite index oluşturulmuştur.

Örnek tahmin geçmişi dokümanı:

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

## Firebase Storage

Tahmin sırasında kullanılan görseller Firebase Storage üzerinde kullanıcıya ait
bir klasör altında saklanır:

```text
prediction-images/{uid}/{predictionId}.{extension}
```

Mobil uygulama Storage'a doğrudan yazmaz. Görsel backend'e gönderilir, backend
Firebase Admin SDK ile Storage'a yükleme yapar ve Firestore tahmin geçmişi
kaydına `imagePath` alanını yazar. Mobil uygulama daha sonra bu yolu Firebase
Storage SDK ile okuyarak görseli ekranda gösterir.

## Hesap Silme

Kullanıcı hesabını silmek istediğinde mobil uygulama önce şifreyle yeniden
doğrulama yapar. Ardından backend üzerinden hesap silme isteği gönderilir.
Backend bu işlem sırasında kullanıcının tahmin geçmişini, profil dokümanını,
Storage üzerindeki görsellerini ve Firebase Auth hesabını temizlemeye çalışır.

Bu yaklaşım, kullanıcıya ait verilerin tek merkezden ve kontrollü biçimde
silinmesini sağlar.

## Testler

Backend tarafında temel unit testler bulunmaktadır. Bu testlerde model sınıf
eşleşmeleri, çiçek katalog bilgileri, tahmin güven değerlendirmesi ve hesap silme
servis akışı kontrol edilir.

Test kapsamı ileride prediction endpointleri, repository hata durumları ve
Firebase entegrasyon senaryoları ile genişletilebilir.
