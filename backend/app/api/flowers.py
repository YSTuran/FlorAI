from fastapi import APIRouter, Depends

from ..auth import CurrentUser, get_current_user
from ..dependencies import get_flower_repository
from ..repositories.flower_repository import FlowerRepository
from ..schemas import FlowerInfo


router = APIRouter()


@router.get("/flowers", response_model=list[FlowerInfo])
async def list_flowers(
    _current_user: CurrentUser = Depends(get_current_user),
    flower_repository: FlowerRepository = Depends(get_flower_repository),
) -> list[FlowerInfo]:
    return flower_repository.list_flowers()
