import unittest

from app.prediction_confidence import assess_prediction_confidence
from app.schemas import PredictionItem


def prediction(label: str, confidence: float) -> PredictionItem:
    return PredictionItem(
        classId=0,
        modelLabel=label,
        flowerId=label,
        displayName=label,
        confidence=confidence,
    )


class PredictionConfidenceTest(unittest.TestCase):
    def test_marks_prediction_as_reliable_when_score_and_gap_are_strong(self):
        result = assess_prediction_confidence(
            [prediction("papatya", 0.91), prediction("lale", 0.12)],
            min_confidence=0.60,
            min_confidence_gap=0.15,
        )

        self.assertFalse(result.low_confidence)
        self.assertEqual(0.79, result.confidence_gap)
        self.assertIsNone(result.confidence_note)

    def test_marks_prediction_as_low_confidence_when_score_is_low(self):
        result = assess_prediction_confidence(
            [prediction("papatya", 0.51), prediction("lale", 0.20)],
            min_confidence=0.60,
            min_confidence_gap=0.15,
        )

        self.assertTrue(result.low_confidence)
        self.assertIn("yeterince emin değil", result.confidence_note or "")

    def test_marks_prediction_as_low_confidence_when_alternative_is_close(self):
        result = assess_prediction_confidence(
            [prediction("papatya", 0.71), prediction("lale", 0.65)],
            min_confidence=0.60,
            min_confidence_gap=0.15,
        )

        self.assertTrue(result.low_confidence)
        self.assertEqual(0.06, result.confidence_gap)
        self.assertIn("kararsız kaldı", result.confidence_note or "")


if __name__ == "__main__":
    unittest.main()
