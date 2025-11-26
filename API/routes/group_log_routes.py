from typing import List

from database import get_db
from fastapi import APIRouter, Depends, HTTPException, Request, status
from repositories.group_log_repository import GroupLogRepository
from repositories.users_groups_repository import UsersGroupsRepository
from schemas.group_log import GroupLogResponse
from services.group_log_service import GroupLogService, IGroupLogService
from services.user_service import UserService
from sqlalchemy.orm import Session

router = APIRouter(tags=["Group Logs"])


def get_current_user_id(request: Request, db: Session = Depends(get_db)) -> int:
    return UserService(db).auth_wrapper(request)


def get_group_log_service(db: Session = Depends(get_db)) -> IGroupLogService:
    repo = GroupLogRepository(db)
    return GroupLogService(repo, db)


def get_user_groups_repo(db: Session = Depends(get_db)):
    return UsersGroupsRepository(db)


@router.get("/{group_id}", response_model=List[GroupLogResponse])
def get_group_logs(
    group_id: int,
    requester_id: int = Depends(get_current_user_id),
    logs_service: IGroupLogService = Depends(get_group_log_service),
    user_groups_repo: UsersGroupsRepository = Depends(get_user_groups_repo),
):
    if not user_groups_repo.is_member(requester_id, group_id):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You are not a member of this group."
        )

    return logs_service.get_logs_for_group(group_id)
