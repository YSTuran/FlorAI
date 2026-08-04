import io
from pathlib import Path

from fastapi import HTTPException, status
from PIL import Image, UnidentifiedImageError
from ultralytics import YOLO

from .flower_catalog import get_flower_by_model_label, get_flower_id_for_model_label
from .schemas import PredictionItem


class FlowerClassifier:
    def __init__(self, model_path: Path, confidence_threshold: float) -> None:
        self.model_path = model_path
        self.confidence_threshold = confidence_threshold
        self._model: YOLO | None = None

    @property
    def is_loaded(self) -> bool:
        return self._model is not None

    @property
    def names(self) -> dict[int, str]:
        if self._model is None:
            return {}
        return dict(self._model.names)

    def load(self) -> None:
        if not self.model_path.exists():
            raise RuntimeError(f"Model file not found: {self.model_path}")
        self._model = YOLO(str(self.model_path))

    def predict(self, image_bytes: bytes, top_k: int) -> list[PredictionItem]:
        if self._model is None:
            raise RuntimeError("Model is not loaded.")

        try:
            image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
        except UnidentifiedImageError as exc:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Uploaded file is not a valid image.",
            ) from exc

        results = self._model.predict(source=image, verbose=False)
        if not results or results[0].probs is None:
            raise RuntimeError("Model did not return classification probabilities.")

        scores = results[0].probs.data.detach().cpu().tolist()
        ranked = sorted(enumerate(scores), key=lambda item: item[1], reverse=True)[:top_k]

        predictions: list[PredictionItem] = []
        for class_id, confidence in ranked:
            model_label = self.names[class_id]
            flower = get_flower_by_model_label(model_label)
            flower_id = get_flower_id_for_model_label(model_label)
            predictions.append(
                PredictionItem(
                    classId=class_id,
                    modelLabel=model_label,
                    flowerId=flower_id,
                    displayName=flower.commonName if flower else model_label,
                    confidence=round(float(confidence), 4),
                )
            )

        return predictions
