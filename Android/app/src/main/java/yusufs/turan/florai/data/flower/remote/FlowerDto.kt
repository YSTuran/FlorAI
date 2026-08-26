package yusufs.turan.florai.data.flower.remote

import yusufs.turan.florai.domain.flower.SupportedFlower

data class FlowerDto(
    val id: String,
    val commonName: String,
    val scientificName: String
) {
    fun toDomain(): SupportedFlower {
        return SupportedFlower(
            id = id,
            displayName = commonName,
            scientificName = scientificName
        )
    }
}
