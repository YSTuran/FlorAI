package yusufs.turan.florai.domain.user

data class AccountDeletionResult(
    val deletedCount: Int,
    val authDeleted: Boolean
)
