from typing import Literal

from pydantic import BaseModel, Field


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


class PredictionResult(BaseModel):
    flowerId: str | None
    classId: int
    modelLabel: str
    name: str
    scientificName: str | None = None
    confidence: float
    lowConfidence: bool
    height: str | None = None
    habitats: list[str] = Field(default_factory=list)
    bloomMonths: list[str] = Field(default_factory=list)
    details: str | None = None
    extraFacts: list[str] = Field(default_factory=list)


class PredictResponse(BaseModel):
    status: Literal["success", "low_confidence"]
    predictionId: str | None = None
    result: PredictionResult


class PredictionHistoryItem(BaseModel):
    id: str
    predictedFlowerId: str | None = None
    displayName: str
    modelLabel: str
    classId: int
    confidence: float
    lowConfidence: bool
    imageUrl: str | None = None
    topPredictions: list[PredictionItem] = Field(default_factory=list)
    createdAt: str | None = None


class PredictionHistoryResponse(BaseModel):
    items: list[PredictionHistoryItem]


class DeleteResponse(BaseModel):
    deletedCount: int


class AppInfoResponse(BaseModel):
    appName: str
    version: str
    description: str
    modelLoaded: bool
    classCount: int
    classes: list[str]
    firestoreEnabled: bool
    endpoints: dict[str, str]


class HealthResponse(BaseModel):
    status: str
    modelLoaded: bool
    classCount: int
    classes: list[str]
    firestoreEnabled: bool
