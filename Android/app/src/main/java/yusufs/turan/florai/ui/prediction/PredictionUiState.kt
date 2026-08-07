package yusufs.turan.florai.ui.prediction

import yusufs.turan.florai.domain.prediction.PredictionResult

data class PredictionUiState(
    val selectedImageName: String? = null,
    val selectedImageSizeBytes: Int? = null,
    val isPredicting: Boolean = false,
    val result: PredictionResult? = null,
    val errorMessage: String? = null
) {
    val hasSelectedImage: Boolean = selectedImageName != null
}
