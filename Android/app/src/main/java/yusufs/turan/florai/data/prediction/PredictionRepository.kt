package yusufs.turan.florai.data.prediction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
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

    fun getReadableMessage(error: Throwable): String {
        return when (error) {
            is HttpException -> when (error.code()) {
                401 -> "Oturum dogrulanamadi. Tekrar giris yap."
                403 -> "Bu islem icin hesabina izin verilmedi."
                413 -> "Gorsel boyutu cok buyuk."
                415 -> "Secilen dosya desteklenen bir gorsel degil."
                else -> "Sunucu yaniti alinamadi. Kod: ${error.code()}"
            }
            is IOException -> "Backend baglantisi kurulamadi."
            else -> error.localizedMessage ?: "Tahmin tamamlanamadi."
        }
    }
}
