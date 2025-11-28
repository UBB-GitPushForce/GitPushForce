from abc import ABC, abstractmethod
from typing import List

from models.group_log import GroupLog
from sqlalchemy import select
from sqlalchemy.orm import Session


class IGroupLogRepository(ABC):

    @abstractmethod
    def add(self, group_id: int, user_id: int, action: str) -> GroupLog: ...

    @abstractmethod
    def get_by_group(self, group_id: int) -> List[GroupLog]: ...


class GroupLogRepository(IGroupLogRepository):

    def __init__(self, db: Session):
        self.db = db

    def add(self, group_id: int, user_id: int, action: str) -> GroupLog:
        log = GroupLog(group_id=group_id, user_id=user_id, action=action)
        self.db.add(log)
        self.db.commit()
        self.db.refresh(log)
        return log

    def get_by_group(self, group_id: int) -> List[GroupLog]:
        statement = (
            select(GroupLog)
            .where(GroupLog.group_id == group_id)
            .order_by(GroupLog.created_at.desc())
        )
        return list(self.db.scalars(statement))
