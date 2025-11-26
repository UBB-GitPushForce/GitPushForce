from abc import ABC, abstractmethod
from typing import List

from fastapi import HTTPException
from sqlalchemy.orm import Session

from models.group import Group
from models.user import User
from models.group_log import GroupLog
from repositories.group_log_repository import IGroupLogRepository


class IGroupLogService(ABC):
    @abstractmethod
    def log_join(self, group_id: int, user_id: int) -> GroupLog: ...
    
    @abstractmethod
    def log_leave(self, group_id: int, user_id: int) -> GroupLog: ...
    
    @abstractmethod
    def get_logs_for_group(self, group_id: int) -> List[GroupLog]: ...


class GroupLogService(IGroupLogService):

    def __init__(self, repo: IGroupLogRepository, db: Session):
        self.repo = repo
        self.db = db

    def _validate_group(self, group_id: int) -> Group:
        group = self.db.query(Group).filter_by(id=group_id).first()
        if not group:
            raise HTTPException(status_code=404, detail="Group not found.")
        return group

    def _validate_user(self, user_id: int) -> User:
        user = self.db.query(User).filter_by(id=user_id).first()
        if not user:
            raise HTTPException(status_code=404, detail="User not found.")
        return user

    def log_join(self, group_id: int, user_id: int) -> GroupLog:
        self._validate_group(group_id)
        self._validate_user(user_id)
        return self.repo.add(group_id, user_id, "JOIN")

    def log_leave(self, group_id: int, user_id: int) -> GroupLog:
        self._validate_group(group_id)
        self._validate_user(user_id)
        return self.repo.add(group_id, user_id, "LEAVE")

    def get_logs_for_group(self, group_id: int) -> List[GroupLog]:
        self._validate_group(group_id)
        return self.repo.get_by_group(group_id)
