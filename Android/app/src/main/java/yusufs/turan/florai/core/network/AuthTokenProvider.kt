package yusufs.turan.florai.core.network

interface AuthTokenProvider {
    fun getToken(): String?
}
