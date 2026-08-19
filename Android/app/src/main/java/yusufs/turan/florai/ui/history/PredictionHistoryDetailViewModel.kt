package yusufs.turan.florai.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yusufs.turan.florai.core.network.NetworkModule
import yusufs.turan.florai.data.prediction.PredictionRepository
import yusufs.turan.florai.domain.prediction.PredictionHistoryItem

class PredictionHistoryDetailViewModel(
    private val predictionRepository: PredictionRepository = NetworkModule.predictionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PredictionHistoryDetailUiState())
    val uiState: StateFlow<PredictionHistoryDetailUiState> = _uiState.asStateFlow()

    private var lastRequestedPredictionId: String? = null

    fun loadDetail(
        predictionId: String,
        cachedItem: PredictionHistoryItem? = null
    ) {
        if (predictionId.isBlank()) {
            _uiState.update {
                it.copy(
                    item = null,
                    isLoading = false,
                    errorMessage = "Tahmin kaydi bulunamadi."
                )
            }
            return
        }

        if (cachedItem != null && cachedItem.id == predictionId) {
            _uiState.update {
                it.copy(item = cachedItem, errorMessage = null)
            }
        }

        if (
            lastRequestedPredictionId == predictionId &&
            _uiState.value.item?.id == predictionId &&
            !_uiState.value.isLoading
        ) {
            return
        }

        lastRequestedPredictionId = predictionId
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            val result = predictionRepository.getPredictionHistoryItem(predictionId)
            _uiState.update { state ->
                if (result.isSuccess) {
                    state.copy(
                        item = result.getOrNull(),
                        isLoading = false,
                        errorMessage = null
                    )
                } else {
                    state.copy(
                        isLoading = false,
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
}
