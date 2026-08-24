from fastapi import APIRouter, Depends, Query

from ..auth import CurrentUser, get_current_user
from ..dependencies import get_prediction_history_service
from ..repositories.common import MAX_HISTORY_ITEMS
from ..schemas import DeleteResponse, PredictionHistoryItem, PredictionHistoryResponse
from ..services.prediction_history_service import PredictionHistoryService


router = APIRouter()


@router.get("/prediction-history", response_model=PredictionHistoryResponse)
async def list_prediction_history(
    limit: int = Query(default=MAX_HISTORY_ITEMS, ge=1, le=MAX_HISTORY_ITEMS),
    cursor: str | None = Query(default=None, min_length=1),
    current_user: CurrentUser = Depends(get_current_user),
    history_service: PredictionHistoryService = Depends(get_prediction_history_service),
) -> PredictionHistoryResponse:
    return history_service.list_history(
        user=current_user,
        limit=limit,
        cursor=cursor,
    )


@router.get("/prediction-history/{prediction_id}", response_model=PredictionHistoryItem)
async def get_prediction_history_item(
    prediction_id: str,
    current_user: CurrentUser = Depends(get_current_user),
    history_service: PredictionHistoryService = Depends(get_prediction_history_service),
) -> PredictionHistoryItem:
    return history_service.get_history_item(
        user=current_user,
        prediction_id=prediction_id,
    )


@router.delete("/prediction-history/{prediction_id}", response_model=DeleteResponse)
async def delete_prediction_history_item(
    prediction_id: str,
    current_user: CurrentUser = Depends(get_current_user),
    history_service: PredictionHistoryService = Depends(get_prediction_history_service),
) -> DeleteResponse:
    return history_service.delete_history_item(
        user=current_user,
        prediction_id=prediction_id,
    )


@router.delete("/prediction-history", response_model=DeleteResponse)
async def delete_prediction_history(
    current_user: CurrentUser = Depends(get_current_user),
    history_service: PredictionHistoryService = Depends(get_prediction_history_service),
) -> DeleteResponse:
    return history_service.delete_history(current_user)
