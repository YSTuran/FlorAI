package yusufs.turan.florai.data.prediction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import yusufs.turan.florai.core.network.HttpErrorParser
import yusufs.turan.florai.data.prediction.remote.PredictionApi
import yusufs.turan.florai.domain.prediction.PredictionHistoryItem
import yusufs.turan.florai.domain.prediction.PredictionHistoryPage
import yusufs.turan.florai.domain.prediction.PredictionResult
import yusufs.turan.florai.domain.prediction.SelectedImage
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException

private const val DEFAULT_HISTORY_LIMIT = 20

class PredictionRepository(
    private val api: PredictionApi
) {
    suspend fun predict(image: SelectedImage): Result<PredictionResult> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = image.bytes.toRequestBody(image.mimeType.toMediaTypeOrNull())
                val multipartImage = MultipartBody.Part.createFormData(
                    name = "image",
                    filename = image.fileName,
                    body = requestBody
                )

                api.predict(multipartImage).toDomain()
            }
        }
    }

    suspend fun getPredictionHistory(
        limit: Int = DEFAULT_HISTORY_LIMIT,
        cursor: String? = null
    ): Result<PredictionHistoryPage> {
        return withContext(Dispatchers.IO) {
            runCatching {
                api.getPredictionHistory(limit, cursor).toDomain()
            }
        }
    }

    suspend fun getPredictionHistoryItem(predictionId: String): Result<PredictionHistoryItem> {
        return withContext(Dispatchers.IO) {
            runCatching {
                api.getPredictionHistoryItem(predictionId).toDomain()
            }
        }
    }

    suspend fun deletePredictionHistoryItem(predictionId: String): Result<Int> {
        return withContext(Dispatchers.IO) {
            runCatching {
                api.deletePredictionHistoryItem(predictionId).deletedCount
            }
        }
    }

    suspend fun deletePredictionHistory(): Result<Int> {
        return withContext(Dispatchers.IO) {
            runCatching {
                api.deletePredictionHistory().deletedCount
            }
        }
    }

    fun getReadableMessage(error: Throwable): String {
        return when (error) {
            is HttpException -> {
                val detail = HttpErrorParser.detail(error)
                when {
                    detail == "Missing Firebase bearer token." ->
                        "Oturum bilgisi bulunamadı. Tekrar giriş yap."
                    detail == "Invalid Firebase token." ->
                        "Oturum süresi dolmuş olabilir. Tekrar giriş yap."
                    detail == "Email verification is required." ->
                        "Tahmin yapmadan önce e-posta adresini doğrulaman gerekiyor."
                    detail == "Only image uploads are supported." ->
                        "Seçilen dosya desteklenen bir görsel değil."
                    detail == "Image file is empty." ->
                        "Görsel dosyası boş görünüyor. Farklı bir fotoğraf dene."
                    detail?.startsWith("Image must be smaller") == true ->
                        "Görsel boyutu çok büyük. Daha küçük bir fotoğraf seç."
                    detail == "Prediction image could not be uploaded." ->
                        "Görsel kaydedilemedi. Storage ayarlarını ve internet bağlantını kontrol et."
                    detail == "Prediction history is not available." ->
                        "Tahmin geçmişi şu anda alınamıyor."
                    detail == "Prediction history index is not available." ->
                        "Tahmin geçmişi için Firestore index'i henüz hazır değil."
                    detail == "Firestore is not available." ->
                        "Firestore bağlantısı şu anda kullanılamıyor."
                    else -> when (error.code()) {
                        400 -> "Görsel okunamadı. Lütfen farklı bir fotoğraf dene."
                        401 -> "Oturum doğrulanamadı. Tekrar giriş yap."
                        403 -> "Bu işlem için hesabına izin verilmedi."
                        404 -> "İlgili kayıt bulunamadı."
                        413 -> "Görsel boyutu çok büyük."
                        415 -> "Seçilen dosya desteklenen bir görsel değil."
                        500 -> "Sunucuda beklenmeyen bir hata oluştu."
                        503 -> "Backend verilerine şu anda ulaşılamıyor."
                        else -> "Sunucu yanıtı alınamadı. Kod: ${error.code()}"
                    }
                }
            }
            is SocketTimeoutException -> "Backend yanıt vermekte gecikti. Lütfen tekrar dene."
            is ConnectException -> "Backend'e ulaşılamadı. Sunucunun çalıştığını ve telefonun aynı ağda olduğunu kontrol et."
            is UnknownHostException -> "Backend adresi bulunamadı. API adresini ve internet bağlantısını kontrol et."
            is IOException -> "Backend bağlantısı kurulamadı. İnternetini ve sunucunun çalıştığını kontrol et."
            else -> error.localizedMessage ?: "İşlem tamamlanamadı."
        }
    }
}
