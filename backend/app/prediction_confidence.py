from dataclasses import dataclass

from .schemas import PredictionItem


@dataclass(frozen=True)
class PredictionConfidenceAssessment:
    low_confidence: bool
    confidence_gap: float | None
    confidence_note: str | None


def assess_prediction_confidence(
    predictions: list[PredictionItem],
    *,
    min_confidence: float,
    min_confidence_gap: float,
) -> PredictionConfidenceAssessment:
    if not predictions:
        return PredictionConfidenceAssessment(
            low_confidence=True,
            confidence_gap=None,
            confidence_note="Model bu görsel için tahmin üretemedi.",
        )

    best_confidence = predictions[0].confidence
    second_confidence = predictions[1].confidence if len(predictions) > 1 else 0.0
    confidence_gap = round(max(0.0, best_confidence - second_confidence), 4)
    has_low_score = best_confidence < min_confidence
    has_close_alternative = (
        len(predictions) > 1
        and confidence_gap < min_confidence_gap
    )

    if has_low_score:
        return PredictionConfidenceAssessment(
            low_confidence=True,
            confidence_gap=confidence_gap,
            confidence_note=(
                "Model bu fotoğraf için yeterince emin değil. Görsel desteklenen "
                "çiçeklerden biri olmayabilir veya fotoğraf yeterince net olmayabilir."
            ),
        )

    if has_close_alternative:
        return PredictionConfidenceAssessment(
            low_confidence=True,
            confidence_gap=confidence_gap,
            confidence_note=(
                "Model bu fotoğrafta iki olası sonuç arasında kararsız kaldı. "
                "Daha net ve çiçeği merkeze alan bir fotoğrafla yeniden deneyebilirsin."
            ),
        )

    return PredictionConfidenceAssessment(
        low_confidence=False,
        confidence_gap=confidence_gap,
        confidence_note=None,
    )
