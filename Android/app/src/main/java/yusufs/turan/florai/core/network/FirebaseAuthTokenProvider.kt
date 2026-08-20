package yusufs.turan.florai.core.network

import yusufs.turan.florai.data.auth.FirebaseAuthRepository

class FirebaseAuthTokenProvider(
    private val authRepository: FirebaseAuthRepository
) : AuthTokenProvider {
    override fun getToken(): String? = authRepository.getIdToken()
}
