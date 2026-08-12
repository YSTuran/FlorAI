package yusufs.turan.florai.data.prediction.remote

import yusufs.turan.florai.domain.prediction.PredictionHistoryItem

data class PredictionHistoryResponseDto(
    val items: List<PredictionHistoryItemDto>
) {
    fun toDomain(): List<PredictionHistoryItem> {
        return items.map { it.toDomain() }
    }
}

data class PredictionHistoryItemDto(
    val id: String,
    val predictedFlowerId: String?,
    val displayName: String,
    val modelLabel: String,
    val classId: Int,
    val confidence: Float,
    val lowConfidence: Boolean,
    val createdAt: String?
) {
    fun toDomain(): PredictionHistoryItem {
        return PredictionHistoryItem(
            id = id,
            predictedFlowerId = predictedFlowerId,
            displayName = displayName,
            modelLabel = modelLabel,
            classId = classId,
            confidence = confidence,
            lowConfidence = lowConfidence,
            createdAt = createdAt
        )
    }
}

data class DeleteResponseDto(
    val deletedCount: Int
)
