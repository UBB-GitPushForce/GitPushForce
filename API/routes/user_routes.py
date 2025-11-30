from dependencies.di import get_user_group_service, get_user_service
from fastapi import APIRouter, Depends, Request
from schemas.user import UserChangePassword, UserUpdate
from services.user_group_service import IUserGroupService
from services.user_service import IUserService
from utils.helpers.jwt_utils import JwtUtils
from utils.helpers.logger import Logger

router = APIRouter(tags=["Users"])
logger = Logger()

def get_current_user_id(request: Request) -> int:
    """
    Returns the authenticated user id.
    """
    return JwtUtils.auth_wrapper(request)

@router.get("/")
def get_all_users(_ = Depends(get_current_user_id), user_service: IUserService = Depends(get_user_service)):
    """
    Returns all users.
    """
    return user_service.get_all_users()


@router.get("/{user_id}")
def get_user_by_id(user_id: int, _ = Depends(get_current_user_id), user_service: IUserService = Depends(get_user_service)):
    """
    Returns a specific user by ID.
    """
    return user_service.get_by_id(user_id)


@router.put("/{user_id}")
def update_user(user_id: int, user_in: UserUpdate, _ = Depends(get_current_user_id), user_service: IUserService = Depends(get_user_service)):
    """
    Updates a user.
    """
    return user_service.update_user(user_id, user_in)

@router.put("/password/change")
def change_password(password_data: UserChangePassword, user_id: int = Depends(get_current_user_id), _ = Depends(get_current_user_id), user_service: IUserService = Depends(get_user_service)):
    """
    Changes the authenticated user's password. Requires old password verification.
    """
    return user_service.change_password(
        user_id=user_id, 
        old_password=password_data.old_password, 
        new_password=password_data.new_password
    )


@router.delete("/{user_id}")
def delete_user(user_id: int, _ = Depends(get_current_user_id), user_service: IUserService = Depends(get_user_service)):
    """
    Deletes a user.
    """
    return user_service.delete_user(user_id)


@router.post("/join-group/{invitation_code}")
def join_group_with_invitation_code(invitation_code: str, user_id: int = Depends(get_current_user_id), user_group_service: IUserGroupService = Depends(get_user_group_service)):
    """
    Allows the authenticated user to join a group using an invitation code.
    """
    return user_group_service.add_user_to_group_by_invitation_code(user_id, invitation_code)

@router.get("/{user_id}/budget")
def get_budget(user_id: int, _ = Depends(get_current_user_id), user_service: IUserService = Depends(get_user_service)):
    return user_service.get_budget(user_id)


@router.put("/{user_id}/budget")
def update_budget(user_id: int, new_budget: int, _ = Depends(get_current_user_id), user_service: IUserService = Depends(get_user_service)):
    return user_service.update_budget(user_id, new_budget)


@router.get("/{user_id}/spent-this-month")
def get_spent_this_month(user_id: int, _ = Depends(get_current_user_id), user_service: IUserService = Depends(get_user_service)):
    return user_service.get_spent_this_month(user_id)


@router.get("/{user_id}/remaining-budget")
def get_remaining_budget(user_id: int, _ = Depends(get_current_user_id), user_service: IUserService = Depends(get_user_service)):
    return user_service.get_remaining_budget(user_id)