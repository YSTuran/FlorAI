package yusufs.turan.florai.domain.prediction

data class PredictionHistoryItem(
    val id: String,
    val predictedFlowerId: String?,
    val displayName: String,
    val modelLabel: String,
    val classId: Int,
    val confidence: Float,
    val lowConfidence: Boolean,
    val imageUrl: String?,
    val topPredictions: List<PredictionScore>,
    val createdAt: String?
)

data class PredictionHistoryPage(
    val items: List<PredictionHistoryItem>,
    val nextCursor: String?
)

data class PredictionScore(
    val classId: Int,
    val modelLabel: String,
    val flowerId: String?,
    val displayName: String,
    val confidence: Float
)
