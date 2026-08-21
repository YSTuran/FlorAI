from fastapi import APIRouter, Depends, File, UploadFile

from ..auth import CurrentUser, get_current_user
from ..dependencies import get_prediction_service
from ..schemas import PredictResponse
from ..services.prediction_service import PredictionService


router = APIRouter()


@router.post("/predict", response_model=PredictResponse)
async def predict(
    image: UploadFile = File(...),
    current_user: CurrentUser = Depends(get_current_user),
    prediction_service: PredictionService = Depends(get_prediction_service),
) -> PredictResponse:
    return await prediction_service.predict(image=image, user=current_user)
