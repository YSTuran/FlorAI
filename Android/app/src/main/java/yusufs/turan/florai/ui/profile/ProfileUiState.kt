package yusufs.turan.florai.ui.profile

import yusufs.turan.florai.domain.user.UserProfile

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val savedProfileVersion: Int = 0
)
