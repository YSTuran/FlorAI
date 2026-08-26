package yusufs.turan.florai.ui.flower

import yusufs.turan.florai.domain.flower.SupportedFlower
import yusufs.turan.florai.domain.flower.SupportedFlowers

data class FlowerCatalogUiState(
    val items: List<SupportedFlower> = SupportedFlowers.items,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
