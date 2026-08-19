package yusufs.turan.florai.ui.history

import yusufs.turan.florai.domain.prediction.PredictionHistoryItem

data class PredictionHistoryDetailUiState(
    val item: PredictionHistoryItem? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
