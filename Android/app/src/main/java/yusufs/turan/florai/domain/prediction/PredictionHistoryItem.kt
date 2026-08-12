package yusufs.turan.florai.domain.prediction

data class PredictionHistoryItem(
    val id: String,
    val predictedFlowerId: String?,
    val displayName: String,
    val modelLabel: String,
    val classId: Int,
    val confidence: Float,
    val lowConfidence: Boolean,
    val createdAt: String?
)
