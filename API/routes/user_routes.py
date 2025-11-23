from database import get_db
from fastapi import APIRouter, Depends, HTTPException, Request
from repositories.users_groups_repository import UsersGroupsRepository
from schemas.user import UserChangePassword, UserUpdate  # <--- Added UserChangePassword
from services.user_service import UserService
from services.users_groups_service import UsersGroupsService
from sqlalchemy.orm import Session
from utils.helpers import logger

router = APIRouter(tags=["Users"])


def get_current_user_id(request: Request, db: Session = Depends(get_db)) -> int:
    """
    Returns the authenticated user id.
    """
    service = UserService(db)
    return service.auth_wrapper(request)


def get_users_groups_service(db: Session = Depends(get_db)) -> UsersGroupsService:
    group_repo = UsersGroupsRepository(db)
    return UsersGroupsService(group_repo)


@router.get("/")
def get_all_users(
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """
    Returns all users (protected).
    """
    service = UserService(db)
    try:
        return service.get_all_users()
    except HTTPException as e:
        raise e
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.get("/{user_id}")
def get_user_by_id(
    user_id: int,
    authenticated_user: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """
    Returns a specific user by ID (protected).
    """
    service = UserService(db)
    try:
        return service.get_user_by_id(user_id)
    except HTTPException as e:
        raise e


@router.put("/{user_id}")
def update_user(
    user_id: int,
    user_in: UserUpdate,
    authenticated_user: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """
    Updates a user (protected).
    """
    service = UserService(db)
    try:
        return service.update_user(user_id, user_in)
    except HTTPException as e:
        logger.Logger().error(e)
        raise e
    except ValueError as e:
        logger.Logger().error(e)
        raise HTTPException(status_code=400, detail=str(e))


@router.put("/password/change")
def change_password(
    password_data: UserChangePassword,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """
    Changes the authenticated user's password. Requires old password verification.
    """
    service = UserService(db)
    try:
        service.change_password(
            user_id=user_id, 
            old_password=password_data.old_password, 
            new_password=password_data.new_password
        )
        return {"message": "Password updated successfully."}
    except HTTPException as e:
        raise e
    except Exception as e:
        logger.Logger().error(e)
        raise HTTPException(status_code=500, detail="An error occurred while updating the password.")


@router.delete("/{user_id}")
def delete_user(
    user_id: int,
    authenticated_user: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """
    Deletes a user (protected).
    """
    service = UserService(db)
    try:
        service.delete_user(user_id)
        return {"message": "User deleted successfully."}
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))


@router.post("/join-group/{invitation_code}")
def join_group_with_invitation_code(
    invitation_code: str,
    user_id: int = Depends(get_current_user_id),
    service: UsersGroupsService = Depends(get_users_groups_service)
):
    """
    Allows the authenticated user to join a group using an invitation code.
    """
    try:
        service.add_user_to_group_by_invitation_code(user_id, invitation_code)
        return {
            "message": "Joined group successfully.",
        }

    except ValueError as e:
        # e.g., invalid invitation code
        raise HTTPException(status_code=400, detail=str(e))

    except Exception as e:
        logger.Logger().error(e)
        raise HTTPException(status_code=500, detail="Cannot join group.")