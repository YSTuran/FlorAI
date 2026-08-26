package yusufs.turan.florai.data.flower

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import yusufs.turan.florai.core.network.HttpErrorParser
import yusufs.turan.florai.data.flower.remote.FlowerApi
import yusufs.turan.florai.domain.flower.SupportedFlower
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class FlowerRepository(
    private val api: FlowerApi
) {
    suspend fun getFlowers(): Result<List<SupportedFlower>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                api.getFlowers().map { it.toDomain() }
            }
        }
    }

    fun getReadableMessage(error: Throwable): String {
        return when (error) {
            is HttpException -> {
                val detail = HttpErrorParser.detail(error)
                when {
                    detail == "Missing Firebase bearer token." ->
                        "Çiçek listesi için oturum bilgisi bulunamadı."
                    detail == "Invalid Firebase token." ->
                        "Oturum süresi dolmuş olabilir. Tekrar giriş yap."
                    detail == "Email verification is required." ->
                        "Çiçek listesini görmek için e-posta doğrulaması gerekiyor."
                    detail == "Firestore is not available." ->
                        "Çiçek bilgileri şu anda kullanılamıyor."
                    else -> "Çiçek listesi alınamadı. Kod: ${error.code()}"
                }
            }
            is SocketTimeoutException -> "Çiçek listesi yanıt vermekte gecikti."
            is ConnectException -> "Backend'e ulaşılamadığı için çiçek listesi güncellenemedi."
            is UnknownHostException -> "Backend adresi bulunamadı."
            is IOException -> "Çiçek listesi için bağlantı kurulamadı."
            else -> error.localizedMessage ?: "Çiçek listesi alınamadı."
        }
    }
}
