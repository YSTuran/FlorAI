package yusufs.turan.florai.data.prediction.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class PredictResponseDtoTest {
    @Test
    fun toDomainMapsConfidenceFields() {
        val result = PredictResponseDto(
            status = "low_confidence",
            predictionId = "prediction-1",
            result = PredictionResultDto(
                flowerId = "papatya",
                classId = 0,
                modelLabel = "daisy",
                name = "Papatya",
                scientificName = "Bellis perennis",
                confidence = 0.57f,
                lowConfidence = true,
                confidenceGap = 0.04f,
                confidenceNote = "Model kararsız kaldı.",
                height = "10-20 cm",
                habitats = listOf("Çayırlar"),
                bloomMonths = listOf("Mart"),
                details = "Detay",
                extraFacts = listOf("Ek bilgi")
            )
        ).toDomain()

        assertEquals("low_confidence", result.status)
        assertEquals("prediction-1", result.predictionId)
        assertEquals(0.04f, result.confidenceGap ?: 0f, 0.001f)
        assertEquals("Model kararsız kaldı.", result.confidenceNote)
    }
}
