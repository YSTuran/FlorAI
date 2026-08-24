package yusufs.turan.florai.ui.history

import yusufs.turan.florai.domain.prediction.PredictionHistoryItem

data class PredictionHistoryUiState(
    val items: List<PredictionHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isDeleting: Boolean = false,
    val nextCursor: String? = null,
    val errorMessage: String? = null
) {
    val hasItems: Boolean = items.isNotEmpty()
    val canLoadMore: Boolean = nextCursor != null && !isLoading && !isLoadingMore && !isDeleting
}
