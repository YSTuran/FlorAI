package yusufs.turan.florai.data.prediction.remote

import yusufs.turan.florai.domain.prediction.PredictionResult

data class PredictResponseDto(
    val status: String,
    val predictionId: String?,
    val result: PredictionResultDto
) {
    fun toDomain(): PredictionResult {
        return PredictionResult(
            status = status,
            predictionId = predictionId,
            flowerId = result.flowerId,
            name = result.name,
            scientificName = result.scientificName,
            confidence = result.confidence,
            lowConfidence = result.lowConfidence,
            confidenceGap = result.confidenceGap,
            confidenceNote = result.confidenceNote?.takeIf { it.isNotBlank() },
            height = result.height,
            habitats = result.habitats.orEmpty(),
            bloomMonths = result.bloomMonths.orEmpty(),
            details = result.details,
            extraFacts = result.extraFacts.orEmpty()
        )
    }
}

data class PredictionResultDto(
    val flowerId: String?,
    val classId: Int,
    val modelLabel: String,
    val name: String,
    val scientificName: String?,
    val confidence: Float,
    val lowConfidence: Boolean,
    val confidenceGap: Float?,
    val confidenceNote: String?,
    val height: String?,
    val habitats: List<String>?,
    val bloomMonths: List<String>?,
    val details: String?,
    val extraFacts: List<String>?
)
