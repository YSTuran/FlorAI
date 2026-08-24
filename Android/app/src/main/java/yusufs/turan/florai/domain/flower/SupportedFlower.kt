package yusufs.turan.florai.domain.flower

data class SupportedFlower(
    val displayName: String,
    val scientificName: String
)

object SupportedFlowers {
    val items = listOf(
        SupportedFlower(displayName = "Papatya", scientificName = "Bellis perennis"),
        SupportedFlower(displayName = "Karahindiba", scientificName = "Taraxacum officinale"),
        SupportedFlower(displayName = "Gül", scientificName = "Rosa spp."),
        SupportedFlower(displayName = "Ayçiçeği", scientificName = "Helianthus annuus"),
        SupportedFlower(displayName = "Lale", scientificName = "Tulipa spp.")
    )
}
