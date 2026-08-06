package yusufs.turan.florai.ui.auth

import yusufs.turan.florai.data.auth.AuthUser

data class AuthUiState(
    val currentUser: AuthUser? = null,
    val isAuthReady: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)
