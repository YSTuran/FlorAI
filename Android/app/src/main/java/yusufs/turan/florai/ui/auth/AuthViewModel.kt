package yusufs.turan.florai.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yusufs.turan.florai.data.auth.FirebaseAuthRepository

class AuthViewModel(
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
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

    fun register(email: String, password: String) {
        submitAuth(email, password, isRegister = true) {
            authRepository.register(email, password, it)
        }
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
        isRegister: Boolean,
        action: ((Result<Unit>) -> Unit) -> Unit
    ) {
        val trimmedEmail = email.trim()
        val validationMessage = validateCredentials(trimmedEmail, password, isRegister)
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
                    isSubmitting = false,
                    errorMessage = result.exceptionOrNull()?.let(authRepository::getReadableMessage)
                )
            }
        }
    }

    private fun validateCredentials(
        email: String,
        password: String,
        isRegister: Boolean
    ): String? {
        if (email.isBlank() || password.isBlank()) {
            return "E-posta ve sifre zorunlu."
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
