package yusufs.turan.florai.data.prediction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import yusufs.turan.florai.domain.prediction.PredictionHistoryItem
import yusufs.turan.florai.data.prediction.remote.PredictionApi
import yusufs.turan.florai.domain.prediction.PredictionResult
import yusufs.turan.florai.domain.prediction.SelectedImage
import java.io.IOException

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

    suspend fun getPredictionHistory(): Result<List<PredictionHistoryItem>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                api.getPredictionHistory().toDomain()
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
            is HttpException -> when (error.code()) {
                400 -> "Gorsel okunamadi. Lutfen farkli bir fotograf dene."
                401 -> "Oturum dogrulanamadi. Tekrar giris yap."
                403 -> "Bu islem icin hesabina izin verilmedi."
                404 -> "Ilgili kayit bulunamadi."
                413 -> "Gorsel boyutu cok buyuk."
                415 -> "Secilen dosya desteklenen bir gorsel degil."
                500 -> "Sunucuda beklenmeyen bir hata olustu."
                503 -> "Firestore baglantisi su anda kullanilamiyor."
                else -> "Sunucu yaniti alinamadi. Kod: ${error.code()}"
            }
            is IOException -> "Backend baglantisi kurulamadi. Internetini ve sunucunun calistigini kontrol et."
            else -> error.localizedMessage ?: "Islem tamamlanamadi."
        }
    }
}
