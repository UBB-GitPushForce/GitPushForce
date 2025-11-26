from io import BytesIO
from typing import List

from database import get_db
from fastapi import APIRouter, Depends, HTTPException, Request, status
from sqlalchemy.exc import NoResultFound
from sqlalchemy.orm import Session
from starlette.responses import StreamingResponse

from repositories.group_repository import GroupRepository
from repositories.users_groups_repository import UsersGroupsRepository
from repositories.group_log_repository import GroupLogRepository
from schemas.expense import Expense
from schemas.group import Group, GroupCreate, GroupUpdate
from schemas.user import UserResponse
from services.expense_service import ExpenseService
from services.group_service import GroupService
from services.group_log_service import GroupLogService
from services.user_service import UserService
from services.users_groups_service import UsersGroupsService
from routes.expense_routes import get_expense_service

router = APIRouter(tags=["Groups"])


def get_current_user_id(request: Request, db: Session = Depends(get_db)) -> int:
    return UserService(db).auth_wrapper(request)


def get_group_service(db: Session = Depends(get_db)) -> GroupService:
    return GroupService(GroupRepository(db))


def get_users_groups_service(db: Session = Depends(get_db)) -> UsersGroupsService:
    return UsersGroupsService(UsersGroupsRepository(db))


def get_group_log_service(db: Session = Depends(get_db)) -> GroupLogService:
    return GroupLogService(GroupLogRepository(db), db)


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
    service: UsersGroupsService = Depends(get_users_groups_service),
    log_service: GroupLogService = Depends(get_group_log_service)
):
    try:
        service.add_user_to_group(user_id, group_id)
        log_service.log_join(group_id, user_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))


@router.delete("/{group_id}/leave", status_code=status.HTTP_204_NO_CONTENT)
def leave_group(
    group_id: int,
    requester_id: int = Depends(get_current_user_id),
    users_groups_service: UsersGroupsService = Depends(get_users_groups_service),
    log_service: GroupLogService = Depends(get_group_log_service),
):
    if not users_groups_service.repository.is_member(requester_id, group_id):
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="You are not a member of this group."
        )

    users_groups_service.delete_user_from_group(requester_id, group_id)
    log_service.log_leave(group_id, requester_id)

    return None


@router.delete("/{group_id}/users/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
def remove_user_from_group(
    group_id: int,
    user_id: int,
    service: UsersGroupsService = Depends(get_users_groups_service),
    log_service: GroupLogService = Depends(get_group_log_service),
):
    try:
        service.delete_user_from_group(user_id, group_id)
        log_service.log_leave(group_id, user_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))


@router.get("/user/{user_id}", response_model=List[Group])
def get_groups_by_user(
    user_id: int,
    service: UsersGroupsService = Depends(get_users_groups_service)
):
    return service.get_user_groups(user_id)


@router.get("/{group_id}/users", response_model=List[UserResponse])
def get_users_by_group(
    group_id: int,
    service: UsersGroupsService = Depends(get_users_groups_service)
):
    try:
        return service.get_users_from_group(group_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))


@router.get("/{group_id}/users/nr", response_model=int)
def get_nr_of_users_from_group(
    group_id: int,
    service: UsersGroupsService = Depends(get_users_groups_service)
):
    try:
        return service.get_nr_of_users_from_group(group_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))


@router.get("/{group_id}/expenses", response_model=List[Expense])
def get_expenses_by_group(
        group_id: int,
        offset: int = 0,
        limit: int = 100,
        sort_by: str = "created_at",
        order: str = "desc",
        service: ExpenseService = Depends(get_expense_service)
):
    try:
        return service.get_group_expenses(group_id, offset, limit, sort_by, order)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))


@router.get("/{group_id}/invite-qr")
def get_group_invite_qr(group_id: int, service: GroupService = Depends(get_group_service)):
    try:
        qr_bytes = service.generate_invite_qr(group_id)
        return StreamingResponse(content=BytesIO(qr_bytes), media_type="image/png")
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))
