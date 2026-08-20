package yusufs.turan.florai.data.prediction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import yusufs.turan.florai.core.network.HttpErrorParser
import yusufs.turan.florai.domain.prediction.PredictionHistoryItem
import yusufs.turan.florai.data.prediction.remote.PredictionApi
import yusufs.turan.florai.domain.prediction.PredictionResult
import yusufs.turan.florai.domain.prediction.SelectedImage
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException

private const val DEFAULT_HISTORY_LIMIT = 50

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
        limit: Int = DEFAULT_HISTORY_LIMIT
    ): Result<List<PredictionHistoryItem>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                api.getPredictionHistory(limit).toDomain()
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
                        "Oturum bilgisi bulunamadi. Tekrar giris yap."
                    detail == "Invalid Firebase token." ->
                        "Oturum suresi dolmus olabilir. Tekrar giris yap."
                    detail == "Email verification is required." ->
                        "Tahmin yapmadan once e-posta adresini dogrulaman gerekiyor."
                    detail == "Only image uploads are supported." ->
                        "Secilen dosya desteklenen bir gorsel degil."
                    detail == "Image file is empty." ->
                        "Gorsel dosyasi bos gorunuyor. Farkli bir fotograf dene."
                    detail?.startsWith("Image must be smaller") == true ->
                        "Gorsel boyutu cok buyuk. Daha kucuk bir fotograf sec."
                    detail == "Prediction image could not be uploaded." ->
                        "Gorsel kaydedilemedi. Storage ayarlarini ve internet baglantini kontrol et."
                    detail == "Prediction history is not available." ->
                        "Tahmin gecmisi su anda alinamiyor."
                    detail == "Firestore is not available." ->
                        "Firestore baglantisi su anda kullanilamiyor."
                    else -> when (error.code()) {
                        400 -> "Gorsel okunamadi. Lutfen farkli bir fotograf dene."
                        401 -> "Oturum dogrulanamadi. Tekrar giris yap."
                        403 -> "Bu islem icin hesabina izin verilmedi."
                        404 -> "Ilgili kayit bulunamadi."
                        413 -> "Gorsel boyutu cok buyuk."
                        415 -> "Secilen dosya desteklenen bir gorsel degil."
                        500 -> "Sunucuda beklenmeyen bir hata olustu."
                        503 -> "Backend verilerine su anda ulasilamiyor."
                        else -> "Sunucu yaniti alinamadi. Kod: ${error.code()}"
                    }
                }
            }
            is SocketTimeoutException -> "Backend yanit vermekte gecikti. Lutfen tekrar dene."
            is ConnectException -> "Backend'e ulasilamadi. Sunucunun calistigini ve telefonun ayni agda oldugunu kontrol et."
            is UnknownHostException -> "Backend adresi bulunamadi. API adresini ve internet baglantisini kontrol et."
            is IOException -> "Backend baglantisi kurulamadi. Internetini ve sunucunun calistigini kontrol et."
            else -> error.localizedMessage ?: "Islem tamamlanamadi."
        }
    }
}
