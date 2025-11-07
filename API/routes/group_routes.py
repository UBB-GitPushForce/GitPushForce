from typing import List

from database import get_db
from fastapi import APIRouter, Depends, HTTPException, Request, status
from repositories.group_repository import GroupRepository
from repositories.users_groups_repository import UsersGroupsRepository
from schemas.group import Group, GroupCreate, GroupUpdate
from schemas.user import UserBase
from services.group_service import GroupService
from services.user_service import UserService
from services.users_groups_service import UsersGroupsService
from sqlalchemy.exc import NoResultFound
from sqlalchemy.orm import Session

router = APIRouter(tags=["Groups"])

def get_current_user_id(request: Request, db: Session = Depends(get_db)) -> int:
    """
    Reads the JWT token from the request (header or cookie) and returns the user ID.
    """
    service = UserService(db)
    return service.auth_wrapper(request)

def get_group_service(db: Session = Depends(get_db)) -> GroupService:
    group_repo = GroupRepository(db)
    return GroupService(group_repo)

def get_users_groups_service(db: Session = Depends(get_db)) -> UsersGroupsService:
    group_repo = UsersGroupsRepository(db)
    return UsersGroupsService(group_repo)

@router.post("/", response_model=Group, status_code=201)
def create_group(group_in: GroupCreate, service: GroupService = Depends(get_group_service)):
    try:
        return service.create_group(group_in)
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))

@router.get("/{group_id}", response_model=Group)
def get_group(group_id: int, service: GroupService = Depends(get_group_service)):
    try:
        return service.get_group_by_id(group_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))

@router.get("/", response_model=List[Group])
def get_all_groups(
        offset: int = 0,
        limit: int = 100,
        service: GroupService = Depends(get_group_service)
):
    return service.get_all_groups(offset=offset, limit=limit)

@router.put("/{group_id}", response_model=Group)
def update_group(
    group_id: int,
    group_in: GroupUpdate,
    service: GroupService = Depends(get_group_service)
):
    try:
        return service.update_group(group_id, group_in)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))

@router.delete("/{group_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_group(
    group_id: int,
    service: GroupService = Depends(get_group_service)
):
    try:
        service.delete_group(group_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))

@router.post("/{group_id}/users/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
def add_user_to_group(
    group_id: int,
    user_id: int,
    service: UsersGroupsService = Depends(get_users_groups_service)
):
    try:
        service.add_user_to_group(user_id, group_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))

@router.delete("/{group_id}/users/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
def remove_user_from_group(
    group_id: int,
    user_id: int,
    service: UsersGroupsService = Depends(get_users_groups_service)
):
    try:
        service.delete_user_from_group(user_id, group_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))

@router.get("/user/{user_id}", response_model=List[Group])
def get_groups_by_user(
    user_id: int,
    service: UsersGroupsService = Depends(get_users_groups_service)
):
    return service.get_user_groups(user_id)

@router.get("/{group_id}/users", response_model=List[UserBase])
def get_users_by_group(
    group_id: int,
    service: UsersGroupsService = Depends(get_users_groups_service)
):
    try:
        return service.get_users_from_group(group_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))
