package yusufs.turan.florai.domain.prediction

data class PredictionResult(
    val status: String,
    val predictionId: String?,
    val flowerId: String?,
    val name: String,
    val scientificName: String?,
    val confidence: Float,
    val lowConfidence: Boolean,
    val height: String?,
    val habitats: List<String>,
    val bloomMonths: List<String>,
    val details: String?,
    val extraFacts: List<String>
)
