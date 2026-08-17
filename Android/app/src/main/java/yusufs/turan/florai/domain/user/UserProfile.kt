package yusufs.turan.florai.domain.user

data class UserProfile(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val role: String,
    val predictionCount: Int,
    val createdAt: String?,
    val updatedAt: String?,
    val lastActiveAt: String?
)
