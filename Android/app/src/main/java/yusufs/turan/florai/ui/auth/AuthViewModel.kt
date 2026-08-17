package yusufs.turan.florai.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yusufs.turan.florai.core.network.NetworkModule
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
        val trimmedDisplayName = displayName.trim()
        val trimmedEmail = email.trim()
        val validationMessage = validateCredentials(
            email = trimmedEmail,
            password = password,
            displayName = trimmedDisplayName,
            isRegister = true
        )
        if (validationMessage != null) {
            _uiState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        _uiState.update {
            it.copy(isSubmitting = true, errorMessage = null)
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
                viewModelScope.launch {
                    val profileResult = userRepository.updateCurrentUserProfile(trimmedDisplayName)
                    _uiState.update { state ->
                        state.copy(
                            currentUser = authRepository.currentUser,
                            isSubmitting = false,
                            errorMessage = profileResult.exceptionOrNull()
                                ?.let(userRepository::getReadableMessage)
                        )
                    }
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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
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
            displayName = displayName,
            isRegister = isRegister
        )
        if (validationMessage != null) {
            _uiState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        _uiState.update {
            it.copy(isSubmitting = true, errorMessage = null)
        }

        action { result ->
            _uiState.update { state ->
                state.copy(
                    currentUser = if (result.isSuccess) {
                        authRepository.currentUser
                    } else {
                        state.currentUser
                    },
                    isSubmitting = false,
                    errorMessage = result.exceptionOrNull()?.let(authRepository::getReadableMessage)
                )
            }
        }
    }

    private fun validateCredentials(
        email: String,
        password: String,
        displayName: String,
        isRegister: Boolean
    ): String? {
        if (email.isBlank() || password.isBlank()) {
            return "E-posta ve sifre zorunlu."
        }

        if (isRegister && displayName.isBlank()) {
            return "Kullanici ismi zorunlu."
        }

        if (isRegister && displayName.length < 2) {
            return "Kullanici ismi en az 2 karakter olmali."
        }

        if (!email.contains("@") || !email.contains(".")) {
            return "Gecerli bir e-posta adresi gir."
        }

        if (isRegister && password.length < 6) {
            return "Sifre en az 6 karakter olmali."
        }

        return null
    }
}
