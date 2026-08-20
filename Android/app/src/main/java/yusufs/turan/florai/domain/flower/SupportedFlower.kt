package yusufs.turan.florai.domain.flower

data class SupportedFlower(
    val displayName: String,
    val scientificName: String
)

object SupportedFlowers {
    val items = listOf(
        SupportedFlower(displayName = "Papatya", scientificName = "Bellis perennis"),
        SupportedFlower(displayName = "Karahindiba", scientificName = "Taraxacum officinale"),
        SupportedFlower(displayName = "Gul", scientificName = "Rosa spp."),
        SupportedFlower(displayName = "Aycicegi", scientificName = "Helianthus annuus"),
        SupportedFlower(displayName = "Lale", scientificName = "Tulipa spp.")
    )
}
