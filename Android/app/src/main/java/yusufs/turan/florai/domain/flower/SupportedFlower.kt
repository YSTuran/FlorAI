package yusufs.turan.florai.domain.flower

data class SupportedFlower(
    val id: String,
    val displayName: String,
    val scientificName: String
)

object SupportedFlowers {
    val items = listOf(
        SupportedFlower(
            id = "papatya",
            displayName = "Papatya",
            scientificName = "Bellis perennis"
        ),
        SupportedFlower(
            id = "karahindiba",
            displayName = "Karahindiba",
            scientificName = "Taraxacum officinale"
        ),
        SupportedFlower(
            id = "gul",
            displayName = "Gül",
            scientificName = "Rosa spp."
        ),
        SupportedFlower(
            id = "aycicegi",
            displayName = "Ayçiçeği",
            scientificName = "Helianthus annuus"
        ),
        SupportedFlower(
            id = "lale",
            displayName = "Lale",
            scientificName = "Tulipa spp."
        )
    )
}
