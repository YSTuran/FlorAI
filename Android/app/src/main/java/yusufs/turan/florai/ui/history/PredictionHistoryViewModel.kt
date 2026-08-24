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

private const val HISTORY_PAGE_LIMIT = 20

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

            _uiState.update {
                it.copy(isLoading = true, isLoadingMore = false, errorMessage = null)
            }

            val result = predictionRepository.getPredictionHistory(limit = HISTORY_PAGE_LIMIT)
            if (requestSessionVersion != sessionVersion) {
                return@launch
            }

            _uiState.update { state ->
                val page = result.getOrNull()
                state.copy(
                    isLoading = false,
                    items = page?.items ?: state.items,
                    nextCursor = if (result.isSuccess) page?.nextCursor else state.nextCursor,
                    errorMessage = result.exceptionOrNull()
                        ?.let(predictionRepository::getReadableMessage)
                )
            }
        }
    }

    fun loadMore() {
        val currentState = _uiState.value
        val cursor = currentState.nextCursor ?: return
        if (!currentState.canLoadMore) return

        val requestSessionVersion = sessionVersion
        viewModelScope.launch {
            if (requestSessionVersion != sessionVersion) {
                return@launch
            }

            _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }

            val result = predictionRepository.getPredictionHistory(
                limit = HISTORY_PAGE_LIMIT,
                cursor = cursor
            )
            if (requestSessionVersion != sessionVersion) {
                return@launch
            }

            _uiState.update { state ->
                val page = result.getOrNull()
                if (page == null) {
                    state.copy(
                        isLoadingMore = false,
                        errorMessage = result.exceptionOrNull()
                            ?.let(predictionRepository::getReadableMessage)
                    )
                } else {
                    val existingIds = state.items.map { it.id }.toSet()
                    state.copy(
                        isLoadingMore = false,
                        items = state.items + page.items.filterNot {
                            it.id in existingIds
                        },
                        nextCursor = page.nextCursor,
                        errorMessage = null
                    )
                }
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
                    state.copy(
                        isDeleting = false,
                        items = emptyList(),
                        nextCursor = null
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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetSessionState() {
        sessionVersion += 1
        _uiState.value = PredictionHistoryUiState()
    }
}
