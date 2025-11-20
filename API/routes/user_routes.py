from fastapi import APIRouter, Depends, HTTPException, Request, Response
from sqlalchemy.orm import Session

from database import get_db
from schemas.user import UserCreate, UserLogin, UserPasswordReset, UserUpdate
from services.user_service import UserService
from utils.helpers import logger

router = APIRouter(tags=["Users"])


def get_current_user_id(request: Request, db: Session = Depends(get_db)) -> int:
    """
    Returns the authenticated user id.
    """
    service = UserService(db)
    return service.auth_wrapper(request)


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
