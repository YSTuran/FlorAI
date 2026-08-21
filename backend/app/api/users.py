from fastapi import APIRouter, Depends

from ..auth import CurrentUser, get_current_user
from ..dependencies import get_user_service
from ..schemas import DeleteResponse, UserProfile, UserProfileUpdate
from ..services.user_service import UserService


router = APIRouter()


@router.get("/users/me", response_model=UserProfile)
async def get_current_user_profile(
    current_user: CurrentUser = Depends(get_current_user),
    user_service: UserService = Depends(get_user_service),
) -> UserProfile:
    return user_service.get_profile(current_user)


@router.put("/users/me", response_model=UserProfile)
async def update_current_user_profile(
    payload: UserProfileUpdate,
    current_user: CurrentUser = Depends(get_current_user),
    user_service: UserService = Depends(get_user_service),
) -> UserProfile:
    return user_service.update_profile(
        user=current_user,
        display_name=payload.displayName,
    )


@router.delete("/users/me", response_model=DeleteResponse)
async def delete_current_user_data(
    current_user: CurrentUser = Depends(get_current_user),
    user_service: UserService = Depends(get_user_service),
) -> DeleteResponse:
    return user_service.delete_current_user_data(current_user)
