package yusufs.turan.florai.ui.prediction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yusufs.turan.florai.core.network.NetworkModule
import yusufs.turan.florai.data.prediction.PredictionRepository
import yusufs.turan.florai.domain.prediction.SelectedImage

class PredictionViewModel(
    private val predictionRepository: PredictionRepository = NetworkModule.predictionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PredictionUiState())
    val uiState: StateFlow<PredictionUiState> = _uiState.asStateFlow()

    private var selectedImage: SelectedImage? = null

    fun setSelectedImage(image: SelectedImage) {
        selectedImage = image
        _uiState.update {
            it.copy(
                selectedImageName = image.fileName,
                selectedImageSizeBytes = image.sizeBytes,
                result = null,
                errorMessage = null
            )
        }
    }

    fun clearSelectedImage() {
        selectedImage = null
        _uiState.value = PredictionUiState()
    }

    fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun predict() {
        val image = selectedImage
        if (image == null) {
            _uiState.update { it.copy(errorMessage = "Once bir gorsel sec.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isPredicting = true, errorMessage = null)
            }

            val result = predictionRepository.predict(image)
            _uiState.update { state ->
                state.copy(
                    isPredicting = false,
                    result = result.getOrNull(),
                    errorMessage = result.exceptionOrNull()
                        ?.let(predictionRepository::getReadableMessage)
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
