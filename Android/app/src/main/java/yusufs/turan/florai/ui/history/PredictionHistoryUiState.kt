package yusufs.turan.florai.ui.history

import yusufs.turan.florai.domain.prediction.PredictionHistoryItem

data class PredictionHistoryUiState(
    val items: List<PredictionHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null
) {
    val hasItems: Boolean = items.isNotEmpty()
}
