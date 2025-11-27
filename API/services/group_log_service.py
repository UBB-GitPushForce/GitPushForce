from abc import ABC, abstractmethod

from fastapi import HTTPException
from models.group import Group
from models.user import User
from repositories.group_log_repository import IGroupLogRepository
from repositories.group_repository import IGroupRepository
from repositories.user_repository import IUserRepository
from schemas.api_response import APIResponse
from schemas.group_log import GroupLogResponse
from utils.helpers.constants import STATUS_NOT_FOUND


class IGroupLogService(ABC):

    @abstractmethod
    def log_join(self, group_id: int, user_id: int) -> APIResponse: ...

    @abstractmethod
    def log_leave(self, group_id: int, user_id: int) -> APIResponse: ...

    @abstractmethod
    def get_logs_for_group(self, user_id: int, group_id: int) -> APIResponse: ...


class GroupLogService(IGroupLogService):

    def __init__(
        self,
        repository: IGroupLogRepository,
        group_repo: IGroupRepository,
        user_repo: IUserRepository,
    ):
        """
        Constructor method.
        """
        self.repository = repository
        self.group_repo = group_repo
        self.user_repo = user_repo

    def _validate_group(self, group_id: int) -> Group:
        group = self.group_repo.get_by_id(group_id)
        if not group:
            raise HTTPException(status_code=STATUS_NOT_FOUND, detail="Group not found.")
        return group

    def _validate_user(self, user_id: int) -> User:
        user = self.user_repo.get_by_id(user_id)
        if not user:
            raise HTTPException(status_code=STATUS_NOT_FOUND, detail="User not found.")
        return user

    def log_join(self, group_id: int, user_id: int) -> APIResponse:
        """
        Logs group join event.
        """
        self._validate_group(group_id)
        self._validate_user(user_id)

        log = self.repository.add(group_id, user_id, "JOIN")
        log_response = GroupLogResponse.model_validate(log)

        return APIResponse(
            success=True, 
            data=log_response
        )

    def log_leave(self, group_id: int, user_id: int) -> APIResponse:
        """
        Logs group leave event.
        """
        self._validate_group(group_id)
        self._validate_user(user_id)

        log = self.repository.add(group_id, user_id, "LEAVE")
        log_response = GroupLogResponse.model_validate(log)

        return APIResponse(
            success=True, 
            data=log_response
        )

    def get_logs_for_group(self, user_id: int, group_id: int) -> APIResponse:
        """
        Retrieves all logs for a group.
        """
        self._validate_group(group_id)
        
        logs = self.repository.get_by_group(group_id)
        logs_response = [GroupLogResponse.model_validate(log) for log in logs]

        return APIResponse(
            success=True, 
            data=logs_response
        )
