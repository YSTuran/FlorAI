package yusufs.turan.florai.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yusufs.turan.florai.data.auth.FirebaseAuthRepository
import yusufs.turan.florai.data.user.UserRepository
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = userRepository.getCurrentUserProfile()
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    profile = result.getOrElse { state.profile },
                    errorMessage = result.exceptionOrNull()
                        ?.let(userRepository::getReadableMessage)
                )
            }
        }
    }

    fun saveDisplayName(displayName: String) {
        val cleanedDisplayName = displayName.trim()
        val validationMessage = validateDisplayName(cleanedDisplayName)
        if (validationMessage != null) {
            _uiState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSaving = true, errorMessage = null, successMessage = null)
            }

            val result = userRepository.updateCurrentUserProfile(cleanedDisplayName)
            if (result.isFailure) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.exceptionOrNull()
                            ?.let(userRepository::getReadableMessage)
                    )
                }
                return@launch
            }

            val authResult = authRepository.updateDisplayName(cleanedDisplayName)
            _uiState.update { state ->
                state.copy(
                    profile = result.getOrNull(),
                    isSaving = false,
                    errorMessage = authResult.exceptionOrNull()
                        ?.let(authRepository::getReadableMessage),
                    successMessage = if (authResult.isSuccess) {
                        "Profil bilgileri kaydedildi."
                    } else {
                        null
                    },
                    savedProfileVersion = if (authResult.isSuccess) {
                        state.savedProfileVersion + 1
                    } else {
                        state.savedProfileVersion
                    }
                )
            }
        }
    }

    fun deleteAccount(password: String) {
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Hesabi silmek icin sifreni girmelisin.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isDeletingAccount = true, errorMessage = null, successMessage = null)
            }

            val reauthResult = authRepository.reauthenticateWithPassword(password)
            if (reauthResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isDeletingAccount = false,
                        errorMessage = reauthResult.exceptionOrNull()
                            ?.let(authRepository::getReadableMessage)
                    )
                }
                return@launch
            }

            val dataResult = userRepository.deleteCurrentUserData()
            if (dataResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isDeletingAccount = false,
                        errorMessage = dataResult.exceptionOrNull()
                            ?.let(userRepository::getReadableMessage)
                    )
                }
                return@launch
            }

            val deleteResult = authRepository.deleteCurrentUser()
            _uiState.update {
                it.copy(
                    profile = if (deleteResult.isSuccess) null else it.profile,
                    isDeletingAccount = false,
                    errorMessage = deleteResult.exceptionOrNull()
                        ?.let(authRepository::getReadableMessage),
                    successMessage = if (deleteResult.isSuccess) {
                        "Hesap silindi."
                    } else {
                        null
                    }
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun validateDisplayName(displayName: String): String? {
        if (displayName.length < 2) {
            return "Kullanici ismi en az 2 karakter olmali."
        }
        if (displayName.length > 40) {
            return "Kullanici ismi en fazla 40 karakter olmali."
        }
        return null
    }
}
