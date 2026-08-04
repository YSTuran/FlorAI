from pydantic import BaseModel


class FlowerInfo(BaseModel):
    id: str
    commonName: str
    scientificName: str
    height: str
    habitats: list[str]
    bloomMonths: list[str]
    details: str
    extraFacts: list[str]


class PredictionItem(BaseModel):
    classId: int
    modelLabel: str
    flowerId: str | None
    displayName: str
    confidence: float


class PredictResponse(BaseModel):
    prediction: PredictionItem
    topPredictions: list[PredictionItem]
    flower: FlowerInfo | None
    lowConfidence: bool
    userId: str


class HealthResponse(BaseModel):
    status: str
    modelLoaded: bool
    classCount: int
    classes: list[str]
