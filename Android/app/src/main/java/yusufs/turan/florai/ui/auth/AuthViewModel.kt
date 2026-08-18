package yusufs.turan.florai.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yusufs.turan.florai.core.network.NetworkModule
import yusufs.turan.florai.data.auth.AuthUser
import yusufs.turan.florai.data.auth.FirebaseAuthRepository
import yusufs.turan.florai.data.user.UserRepository

class AuthViewModel(
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository(),
    private val userRepository: UserRepository = NetworkModule.userRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AuthUiState(currentUser = authRepository.currentUser)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isAuthReady = true,
                        isSubmitting = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        submitAuth(email, password, isRegister = false) {
            authRepository.signIn(email, password, it)
        }
    }

    fun register(displayName: String, email: String, password: String) {
        register(displayName, email, password, password)
    }

    fun register(
        displayName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        val trimmedDisplayName = displayName.trim()
        val trimmedEmail = email.trim()
        val validationMessage = validateCredentials(
            email = trimmedEmail,
            password = password,
            confirmPassword = confirmPassword,
            displayName = trimmedDisplayName,
            isRegister = true
        )
        if (validationMessage != null) {
            _uiState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        _uiState.update {
            it.copy(isSubmitting = true, errorMessage = null, successMessage = null)
        }

        authRepository.register(trimmedDisplayName, trimmedEmail, password) { authResult ->
            if (authResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = authResult.exceptionOrNull()
                            ?.let(authRepository::getReadableMessage)
                    )
                }
            } else {
                _uiState.update { state ->
                    state.copy(
                        currentUser = authRepository.currentUser,
                        isSubmitting = false,
                        successMessage = "Dogrulama e-postasi gonderildi."
                    )
                }
            }
        }
    }

    fun refreshCurrentUser() {
        _uiState.update { it.copy(currentUser = authRepository.currentUser) }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun sendPasswordResetEmail(email: String) {
        val trimmedEmail = email.trim()
        val validationMessage = validateEmail(trimmedEmail)
        if (validationMessage != null) {
            _uiState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        _uiState.update {
            it.copy(isSubmitting = true, errorMessage = null, successMessage = null)
        }

        authRepository.sendPasswordResetEmail(trimmedEmail) { result ->
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    errorMessage = result.exceptionOrNull()
                        ?.let(authRepository::getReadableMessage),
                    successMessage = if (result.isSuccess) {
                        "Sifre sifirlama e-postasi gonderildi."
                    } else {
                        null
                    }
                )
            }
        }
    }

    fun resendEmailVerification() {
        _uiState.update {
            it.copy(isSubmitting = true, errorMessage = null, successMessage = null)
        }

        authRepository.sendEmailVerification { result ->
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    errorMessage = result.exceptionOrNull()
                        ?.let(authRepository::getReadableMessage),
                    successMessage = if (result.isSuccess) {
                        "Dogrulama e-postasi tekrar gonderildi."
                    } else {
                        null
                    }
                )
            }
        }
    }

    fun refreshEmailVerification() {
        _uiState.update {
            it.copy(isSubmitting = true, errorMessage = null, successMessage = null)
        }

        authRepository.refreshCurrentUser { result ->
            val currentUser = authRepository.currentUser
            if (result.isSuccess && currentUser?.emailVerified == true) {
                viewModelScope.launch {
                    val profileResult = ensureProfileAfterVerification(currentUser)
                    _uiState.update {
                        it.copy(
                            currentUser = currentUser,
                            isSubmitting = false,
                            errorMessage = profileResult.exceptionOrNull()
                                ?.let(userRepository::getReadableMessage),
                            successMessage = if (profileResult.isSuccess) {
                                "E-posta adresi dogrulandi."
                            } else {
                                null
                            }
                        )
                    }
                }
                return@refreshCurrentUser
            }

            _uiState.update {
                it.copy(
                    currentUser = currentUser,
                    isSubmitting = false,
                    errorMessage = when {
                        result.isFailure -> result.exceptionOrNull()
                            ?.let(authRepository::getReadableMessage)
                        currentUser?.emailVerified == false -> "E-posta adresi henuz dogrulanmamis."
                        else -> null
                    },
                    successMessage = if (result.isSuccess && currentUser?.emailVerified == true) {
                        "E-posta adresi dogrulandi."
                    } else {
                        null
                    }
                )
            }
        }
    }

    private suspend fun ensureProfileAfterVerification(user: AuthUser): Result<Unit> {
        return userRepository.updateCurrentUserProfile(resolveProfileDisplayName(user))
            .map { Unit }
    }

    private fun resolveProfileDisplayName(user: AuthUser): String {
        val candidates = listOfNotNull(
            user.displayName,
            user.email?.substringBefore("@")?.replace(Regex("[._-]+"), " ")
        )

        return candidates
            .map { it.trim() }
            .firstOrNull { it.length >= 2 }
            ?.take(40)
            ?: "FlorAI kullanicisi"
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun submitAuth(
        email: String,
        password: String,
        displayName: String = "",
        isRegister: Boolean,
        action: ((Result<Unit>) -> Unit) -> Unit
    ) {
        val trimmedEmail = email.trim()
        val validationMessage = validateCredentials(
            email = trimmedEmail,
            password = password,
            confirmPassword = password,
            displayName = displayName,
            isRegister = isRegister
        )
        if (validationMessage != null) {
            _uiState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        _uiState.update {
            it.copy(isSubmitting = true, errorMessage = null, successMessage = null)
        }

        action { result ->
            val currentUser = authRepository.currentUser
            _uiState.update { state ->
                state.copy(
                    currentUser = if (result.isSuccess) {
                        currentUser
                    } else {
                        state.currentUser
                    },
                    isSubmitting = false,
                    errorMessage = result.exceptionOrNull()?.let(authRepository::getReadableMessage)
                        ?: if (result.isSuccess && currentUser?.emailVerified == false) {
                            "E-posta adresini dogrulaman gerekiyor."
                        } else {
                            null
                        }
                )
            }
        }
    }

    private fun validateCredentials(
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String,
        isRegister: Boolean
    ): String? {
        if (email.isBlank() || password.isBlank()) {
            return "E-posta ve sifre zorunlu."
        }

        validateEmail(email)?.let { return it }

        if (isRegister && displayName.isBlank()) {
            return "Kullanici ismi zorunlu."
        }

        if (isRegister && displayName.length < 2) {
            return "Kullanici ismi en az 2 karakter olmali."
        }

        if (isRegister && password.length < 6) {
            return "Sifre en az 6 karakter olmali."
        }

        if (isRegister && password != confirmPassword) {
            return "Sifreler eslesmiyor."
        }

        return null
    }

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) {
            return "E-posta zorunlu."
        }

        if (!email.contains("@") || !email.contains(".")) {
            return "Gecerli bir e-posta adresi gir."
        }

        return null
    }
}
