
from dependencies.di import (
    get_expense_service,
    get_group_log_service,
    get_group_service,
    get_user_group_service,
)
from fastapi import APIRouter, Depends
from schemas.group import GroupCreate, GroupUpdate
from services.expense_service import IExpenseService
from services.group_log_service import GroupLogService, IGroupLogService
from services.group_service import IGroupService
from services.user_group_service import IUserGroupService
from utils.helpers.jwt_utils import JwtUtils

router = APIRouter(tags=["Groups"])


@router.post("/")
def create_group(group_in: GroupCreate, group_service: IGroupService = Depends(get_group_service)):
    """
    Creates group.
    """
    return group_service.create_group(group_in)


@router.get("/{group_id}")
def get_group(group_id: int, service: IGroupService = Depends(get_group_service)):
    """
    Returns group by id.
    """
    return service.get_group_by_id(group_id)


@router.get("/")
def get_all_groups(offset: int = 0, limit: int = 100, group_service: IGroupService = Depends(get_group_service)):
    """
    Returns all groups.
    """
    return group_service.get_all_groups(offset=offset, limit=limit)


@router.put("/{group_id}")
def update_group(group_id: int, group_in: GroupUpdate, group_service: IGroupService = Depends(get_group_service)):
    """
    Updates group.
    """
    return group_service.update_group(group_id, group_in)

@router.delete("/{group_id}")
def delete_group(group_id: int, group_service: IGroupService = Depends(get_group_service)):
    """
    Deletes a group.
    """
    return group_service.delete_group(group_id)


@router.post("/{group_id}/users/{user_id}")
def add_user_to_group(
    group_id: int,
    user_id: int,
    user_group_service: IUserGroupService = Depends(get_user_group_service),
    log_service: IGroupLogService = Depends(get_group_log_service)
):
    """
    Adds user to group.
    """
    response = user_group_service.add_user_to_group(user_id, group_id)
    
    if response.success is True:
        log_service.log_join(group_id, user_id)
        
    return response


@router.delete("/{group_id}/leave")
def leave_group(
    group_id: int,
    requester_id: int = Depends(JwtUtils.auth_wrapper),
    user_group_service: IUserGroupService = Depends(get_user_group_service),
    log_service: GroupLogService = Depends(get_group_log_service),
):
    """
    Takes user out of group (this would be the route you theoretically call as an user)
    """

    response = user_group_service.delete_user_from_group(requester_id, group_id)
    if response.success:
        log_service.log_leave(group_id, requester_id)

    return response


@router.delete("/{group_id}/users/{user_id}")
def remove_user_from_group(
    group_id: int,
    user_id: int,
    user_group_service: IUserGroupService = Depends(get_user_group_service),
    log_service: IGroupLogService = Depends(get_group_log_service),
):
    """
    Takes user out of group (this is more of an admin route, use the one above for leaving a group)
    """
    response = user_group_service.delete_user_from_group(user_id, group_id)
    if response.success:
        log_service.log_leave(group_id, user_id)
    
    return response


@router.get("/user/{user_id}")
def get_groups_by_user(
    user_id: int,
    user_group_service: IUserGroupService = Depends(get_user_group_service)
):
    """
    Returns the groups a user is part of.
    """
    return user_group_service.get_user_groups(user_id)


@router.get("/{group_id}/users")
def get_users_by_group(
    group_id: int,
    user_group_service: IUserGroupService = Depends(get_user_group_service)
):
    """
    Returns all users in a group
    """
    return user_group_service.get_users_from_group(group_id)


@router.get("/{group_id}/users/nr")
def get_nr_of_users_from_group(
    group_id: int,
    user_group_service: IUserGroupService = Depends(get_user_group_service)
):
    """
    Returns number of users in a group
    """
    return user_group_service.get_nr_of_users_from_group(group_id)


@router.get("/{group_id}/expenses")
def get_expenses_by_group(
        group_id: int,
        offset: int = 0,
        limit: int = 100,
        sort_by: str = "created_at",
        order: str = "desc",
        expense_service: IExpenseService = Depends(get_expense_service)
):
    """
    Returns expenses by group
    """
    return expense_service.get_group_expenses(group_id, offset, limit, sort_by, order)


@router.get("/{group_id}/invite-qr")
def get_group_invite_qr(group_id: int, group_service: IGroupService = Depends(get_group_service)):
    """
    Returns QR code for joining group
    """
    return group_service.generate_invite_qr(group_id)


@router.get("/{group_id}/statistics/user-summary")
def get_user_group_statistics(
    group_id: int,
    user_id: int = Depends(JwtUtils.auth_wrapper),
    expense_service: IExpenseService = Depends(get_expense_service)
):
    """
    Returns statistics for the authenticated user within a specific group.
    """
    return expense_service.get_user_group_statistics(user_id, group_id)