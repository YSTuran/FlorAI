package yusufs.turan.florai.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yusufs.turan.florai.data.prediction.PredictionRepository
import javax.inject.Inject

@HiltViewModel
class PredictionHistoryViewModel @Inject constructor(
    private val predictionRepository: PredictionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PredictionHistoryUiState())
    val uiState: StateFlow<PredictionHistoryUiState> = _uiState.asStateFlow()

    private var sessionVersion: Int = 0

    fun loadHistory() {
        val requestSessionVersion = sessionVersion
        viewModelScope.launch {
            if (requestSessionVersion != sessionVersion) {
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = predictionRepository.getPredictionHistory()
            if (requestSessionVersion != sessionVersion) {
                return@launch
            }

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    items = result.getOrElse { state.items },
                    errorMessage = result.exceptionOrNull()
                        ?.let(predictionRepository::getReadableMessage)
                )
            }
        }
    }

    fun deleteItem(predictionId: String) {
        val requestSessionVersion = sessionVersion
        viewModelScope.launch {
            if (requestSessionVersion != sessionVersion) {
                return@launch
            }

            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }

            val result = predictionRepository.deletePredictionHistoryItem(predictionId)
            if (requestSessionVersion != sessionVersion) {
                return@launch
            }

            _uiState.update { state ->
                if (result.isSuccess) {
                    state.copy(
                        isDeleting = false,
                        items = state.items.filterNot { it.id == predictionId }
                    )
                } else {
                    state.copy(
                        isDeleting = false,
                        errorMessage = result.exceptionOrNull()
                            ?.let(predictionRepository::getReadableMessage)
                    )
                }
            }
        }
    }

    fun deleteAll() {
        val requestSessionVersion = sessionVersion
        viewModelScope.launch {
            if (requestSessionVersion != sessionVersion) {
                return@launch
            }

            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }

            val result = predictionRepository.deletePredictionHistory()
            if (requestSessionVersion != sessionVersion) {
                return@launch
            }

            _uiState.update { state ->
                if (result.isSuccess) {
                    state.copy(isDeleting = false, items = emptyList())
                } else {
                    state.copy(
                        isDeleting = false,
                        errorMessage = result.exceptionOrNull()
                            ?.let(predictionRepository::getReadableMessage)
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetSessionState() {
        sessionVersion += 1
        _uiState.value = PredictionHistoryUiState()
    }
}
