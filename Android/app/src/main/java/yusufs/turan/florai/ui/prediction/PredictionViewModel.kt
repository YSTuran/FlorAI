package yusufs.turan.florai.ui.prediction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yusufs.turan.florai.data.prediction.PredictionRepository
import yusufs.turan.florai.domain.prediction.SelectedImage
import javax.inject.Inject

@HiltViewModel
class PredictionViewModel @Inject constructor(
    private val predictionRepository: PredictionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PredictionUiState())
    val uiState: StateFlow<PredictionUiState> = _uiState.asStateFlow()

    private var selectedImage: SelectedImage? = null
    private var selectedImageKey: String? = null
    private var lastPredictedImageKey: String? = null
    private var sessionVersion: Int = 0

    fun setSelectedImage(image: SelectedImage) {
        val imageKey = image.key()
        selectedImage = image
        selectedImageKey = imageKey
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
        resetSelectedImage()
        _uiState.value = PredictionUiState()
    }

    fun resetSessionState() {
        sessionVersion += 1
        resetSelectedImage()
        _uiState.value = PredictionUiState()
    }

    private fun resetSelectedImage() {
        selectedImage = null
        selectedImageKey = null
        lastPredictedImageKey = null
    }

    fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun predict() {
        val image = selectedImage
        if (image == null) {
            _uiState.update { it.copy(errorMessage = "Önce bir görsel seç.") }
            return
        }

        if (selectedImageKey == lastPredictedImageKey && _uiState.value.result != null) {
            _uiState.update {
                it.copy(errorMessage = "Bu görsel için tahmin zaten yapıldı. Yeni bir görsel seçerek devam edebilirsin.")
            }
            return
        }

        val requestSessionVersion = sessionVersion
        viewModelScope.launch {
            if (requestSessionVersion != sessionVersion) {
                return@launch
            }

            _uiState.update {
                it.copy(isPredicting = true, errorMessage = null)
            }

            val result = predictionRepository.predict(image)
            if (requestSessionVersion != sessionVersion) {
                return@launch
            }

            if (result.isSuccess) {
                lastPredictedImageKey = selectedImageKey
            }
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

    private fun SelectedImage.key(): String {
        return "$fileName:$sizeBytes:${bytes.contentHashCode()}"
    }
}
