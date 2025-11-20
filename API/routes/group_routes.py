from typing import List

from database import get_db
from fastapi import APIRouter, Depends, HTTPException, Request, status
from repositories.group_repository import GroupRepository
from repositories.users_groups_repository import UsersGroupsRepository
from schemas.expense import Expense
from schemas.group import Group, GroupCreate, GroupUpdate
from schemas.user import UserResponse
from services.expense_service import ExpenseService
from services.group_service import GroupService
from services.user_service import UserService
from services.users_groups_service import UsersGroupsService
from sqlalchemy.exc import NoResultFound
from sqlalchemy.orm import Session

from routes.expense_routes import get_expense_service

router = APIRouter(tags=["Groups"])


def get_current_user_id(request: Request, db: Session = Depends(get_db)) -> int:
    """
    Returns the authenticated user id.

    Args:
        request (Request) incoming request with authentication data
        db (Session) database session

    Returns:
        int id of the authenticated user

    Exceptions:
        HTTPException returned when token is invalid
    """
    service = UserService(db)
    return service.auth_wrapper(request)


def get_group_service(db: Session = Depends(get_db)) -> GroupService:
    """
    Returns a group service instance.

    Args:
        db (Session) database session

    Returns:
        GroupService service for group operations

    Exceptions:
        None
    """
    group_repo = GroupRepository(db)
    return GroupService(group_repo)


def get_users_groups_service(db: Session = Depends(get_db)) -> UsersGroupsService:
    """
    Returns the users-groups service instance.

    Args:
        db (Session) database session

    Returns:
        UsersGroupsService service for linking users and groups

    Exceptions:
        None
    """
    group_repo = UsersGroupsRepository(db)
    return UsersGroupsService(group_repo)


@router.post("/", response_model=Group, status_code=201)
def create_group(group_in: GroupCreate, service: GroupService = Depends(get_group_service)):
    """
    Creates a new group.

    Args:
        group_in (GroupCreate) group data to create
        service (GroupService) group service instance

    Returns:
        Group created group object

    Exceptions:
        HTTPException returned when input is invalid
    """
    try:
        return service.create_group(group_in)
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))


@router.get("/{group_id}", response_model=Group)
def get_group(group_id: int, service: GroupService = Depends(get_group_service)):
    """
    Retrieves a group by id.

    Args:
        group_id (int) id of the group
        service (GroupService) group service instance

    Returns:
        Group matching group object

    Exceptions:
        HTTPException returned when group is not found
    """
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
    """
    Retrieves all groups.

    Args:
        offset (int) items to skip
        limit (int) maximum items to return
        service (GroupService) group service instance

    Returns:
        list[Group] paginated list of groups

    Exceptions:
        None
    """
    return service.get_all_groups(offset=offset, limit=limit)


@router.put("/{group_id}", response_model=Group)
def update_group(
    group_id: int,
    group_in: GroupUpdate,
    service: GroupService = Depends(get_group_service)
):
    """
    Updates a group by id.

    Args:
        group_id (int) id of the group
        group_in (GroupUpdate) updated fields
        service (GroupService) group service instance

    Returns:
        Group updated group object

    Exceptions:
        HTTPException returned when group is not found or data is invalid
    """
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
    """
    Deletes a group by id.

    Args:
        group_id (int) id of the group
        service (GroupService) group service instance

    Returns:
        None no content

    Exceptions:
        HTTPException returned when group is not found
    """
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
    """
    Adds a user to a group.

    Args:
        group_id (int) id of the group
        user_id (int) id of the user
        service (UsersGroupsService) linking service

    Returns:
        None no content

    Exceptions:
        HTTPException returned when user or group is not found
    """
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
    """
    Removes a user from a group.

    Args:
        group_id (int) id of the group
        user_id (int) id of the user
        service (UsersGroupsService) linking service

    Returns:
        None no content

    Exceptions:
        HTTPException returned when relationship is not found
    """
    try:
        service.delete_user_from_group(user_id, group_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))


@router.get("/user/{user_id}", response_model=List[Group])
def get_groups_by_user(
    user_id: int,
    service: UsersGroupsService = Depends(get_users_groups_service)
):
    """
    Retrieves all groups a user belongs to.

    Args:
        user_id (int) id of the user
        service (UsersGroupsService) linking service

    Returns:
        list[Group] groups the user is part of

    Exceptions:
        None
    """
    return service.get_user_groups(user_id)


@router.get("/{group_id}/users", response_model=List[UserResponse])
def get_users_by_group(
    group_id: int,
    service: UsersGroupsService = Depends(get_users_groups_service)
):
    """
    Retrieves all users in a group.

    Args:
        group_id (int) id of the group
        service (UsersGroupsService) linking service

    Returns:
        list[UserBase] list of users in the group

    Exceptions:
        HTTPException returned when group is not found
    """
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
    """
    Retrieves all expenses linked to a group.

    Args:
        group_id (int) id of the group
        offset (int) items to skip
        limit (int) maximum items to return
        sort_by (str) sorting field
        order (str) sorting direction
        service (ExpenseService) expense service instance

    Returns:
        list[Expense] group expenses list

    Exceptions:
        HTTPException returned when group is not found
    """
    try:
        return service.get_group_expenses(group_id, offset, limit, sort_by, order)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))
