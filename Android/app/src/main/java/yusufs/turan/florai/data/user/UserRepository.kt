package yusufs.turan.florai.data.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import yusufs.turan.florai.data.user.remote.UpdateUserProfileRequestDto
import yusufs.turan.florai.data.user.remote.UserApi
import yusufs.turan.florai.domain.user.UserProfile
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class UserRepository(
    private val api: UserApi
) {
    suspend fun getCurrentUserProfile(): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            runCatching {
                api.getCurrentUserProfile().toDomain()
            }
        }
    }

    suspend fun updateCurrentUserProfile(displayName: String): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            runCatching {
                api.updateCurrentUserProfile(
                    UpdateUserProfileRequestDto(displayName = displayName)
                ).toDomain()
            }
        }
    }

    fun getReadableMessage(error: Throwable): String {
        return when (error) {
            is HttpException -> when (error.code()) {
                400 -> "Profil bilgileri gecersiz."
                401 -> "Oturum dogrulanamadi. Tekrar giris yap."
                403 -> "Bu profil icin yetkin yok."
                422 -> "Kullanici ismi 2-40 karakter arasinda olmali."
                500 -> "Sunucuda beklenmeyen bir hata olustu."
                503 -> "Profil bilgileri su anda kullanilamiyor."
                else -> "Sunucu yaniti alinamadi. Kod: ${error.code()}"
            }
            is SocketTimeoutException -> "Backend yanit vermekte gecikti. Lutfen tekrar dene."
            is ConnectException -> "Backend'e ulasilamadi. Sunucunun calistigini kontrol et."
            is UnknownHostException -> "Backend adresi bulunamadi. Internet baglantisini kontrol et."
            is IOException -> "Backend baglantisi kurulamadi. Internetini kontrol et."
            else -> error.localizedMessage ?: "Islem tamamlanamadi."
        }
    }
}
