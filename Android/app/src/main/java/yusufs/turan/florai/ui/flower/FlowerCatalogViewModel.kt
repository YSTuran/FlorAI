package yusufs.turan.florai.ui.flower

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yusufs.turan.florai.data.flower.FlowerRepository
import javax.inject.Inject

@HiltViewModel
class FlowerCatalogViewModel @Inject constructor(
    private val flowerRepository: FlowerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FlowerCatalogUiState())
    val uiState: StateFlow<FlowerCatalogUiState> = _uiState.asStateFlow()

    private var didLoad = false
    private var sessionVersion = 0

    fun loadFlowers(forceRefresh: Boolean = false) {
        if (didLoad && !forceRefresh) return

        val requestSessionVersion = sessionVersion
        viewModelScope.launch {
            if (requestSessionVersion != sessionVersion) {
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = flowerRepository.getFlowers()
            if (requestSessionVersion != sessionVersion) {
                return@launch
            }

            didLoad = result.isSuccess
            _uiState.update { state ->
                state.copy(
                    items = result.getOrNull()?.takeIf { it.isNotEmpty() } ?: state.items,
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()
                        ?.let(flowerRepository::getReadableMessage)
                )
            }
        }
    }

    fun resetSessionState() {
        sessionVersion += 1
        didLoad = false
        _uiState.value = FlowerCatalogUiState()
    }
}
