package yusufs.turan.florai.data.prediction.remote

import yusufs.turan.florai.domain.prediction.PredictionHistoryItem
import yusufs.turan.florai.domain.prediction.PredictionHistoryPage
import yusufs.turan.florai.domain.prediction.PredictionScore

data class PredictionHistoryResponseDto(
    val items: List<PredictionHistoryItemDto>,
    val nextCursor: String?
) {
    fun toDomain(): PredictionHistoryPage {
        return PredictionHistoryPage(
            items = items.map { it.toDomain() },
            nextCursor = nextCursor?.takeIf { it.isNotBlank() }
        )
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
    val imageUrl: String?,
    val topPredictions: List<PredictionScoreDto>?,
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
            imageUrl = imageUrl?.takeIf { it.isNotBlank() },
            topPredictions = topPredictions.orEmpty().map { it.toDomain() },
            createdAt = createdAt
        )
    }
}

data class PredictionScoreDto(
    val classId: Int,
    val modelLabel: String,
    val flowerId: String?,
    val displayName: String,
    val confidence: Float
) {
    fun toDomain(): PredictionScore {
        return PredictionScore(
            classId = classId,
            modelLabel = modelLabel,
            flowerId = flowerId,
            displayName = displayName,
            confidence = confidence
        )
    }
}

data class DeleteResponseDto(
    val deletedCount: Int
)
