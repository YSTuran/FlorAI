package yusufs.turan.florai.data.user.remote

import yusufs.turan.florai.domain.user.AccountDeletionResult
import yusufs.turan.florai.domain.user.UserProfile

data class UserProfileDto(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val role: String?,
    val predictionCount: Int?,
    val createdAt: String?,
    val updatedAt: String?,
    val lastActiveAt: String?
) {
    fun toDomain(): UserProfile {
        return UserProfile(
            uid = uid,
            email = email,
            displayName = displayName?.takeIf { it.isNotBlank() },
            role = role ?: "user",
            predictionCount = predictionCount ?: 0,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastActiveAt = lastActiveAt
        )
    }
}

data class UpdateUserProfileRequestDto(
    val displayName: String
)

data class DeleteUserDataResponseDto(
    val deletedCount: Int,
    val authDeleted: Boolean?
) {
    fun toDomain(): AccountDeletionResult {
        return AccountDeletionResult(
            deletedCount = deletedCount,
            authDeleted = authDeleted ?: false
        )
    }
}
