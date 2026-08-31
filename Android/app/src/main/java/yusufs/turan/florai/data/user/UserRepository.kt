package yusufs.turan.florai.data.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import yusufs.turan.florai.core.network.HttpErrorParser
import yusufs.turan.florai.data.user.remote.UpdateUserProfileRequestDto
import yusufs.turan.florai.data.user.remote.UserApi
import yusufs.turan.florai.domain.user.AccountDeletionResult
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

    suspend fun deleteCurrentAccount(): Result<AccountDeletionResult> {
        return withContext(Dispatchers.IO) {
            runCatching {
                api.deleteCurrentUserData().toDomain()
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
                    detail == "Display name is required." ->
                        "Kullanıcı ismi boş bırakılamaz."
                    detail == "User profile is not available." ->
                        "Profil bilgileri şu anda kullanılamıyor."
                    detail == "User data could not be deleted." ->
                        "Kullanıcı verileri silinemedi. Lütfen tekrar dene."
                    detail == "Firebase Auth account could not be deleted." ->
                        "Kullanıcı hesabı silinemedi. Lütfen tekrar dene."
                    detail == "Recent authentication is required." ->
                        "Hesabı silmek için şifrenle tekrar doğrulama gerekiyor."
                    detail == "Prediction history is not available." ->
                        "Tahmin geçmişi şu anda temizlenemiyor."
                    detail == "Firestore is not available." ->
                        "Firestore bağlantısı şu anda kullanılamıyor."
                    else -> when (error.code()) {
                        400 -> "Profil bilgileri geçersiz."
                        401 -> "Oturum doğrulanamadı. Tekrar giriş yap."
                        403 -> "Bu profil için yetkin yok."
                        422 -> "Kullanıcı ismi 2-40 karakter arasında olmalı."
                        500 -> "Sunucuda beklenmeyen bir hata oluştu."
                        503 -> "Profil bilgileri şu anda kullanılamıyor."
                        else -> "Sunucu yanıtı alınamadı. Kod: ${error.code()}"
                    }
                }
            }
            is SocketTimeoutException -> "Backend yanıt vermekte gecikti. Lütfen tekrar dene."
            is ConnectException -> "Backend'e ulaşılamadı. Sunucunun çalıştığını kontrol et."
            is UnknownHostException -> "Backend adresi bulunamadı. İnternet bağlantısını kontrol et."
            is IOException -> "Backend bağlantısı kurulamadı. İnternetini kontrol et."
            else -> error.localizedMessage ?: "İşlem tamamlanamadı."
        }
    }
}
