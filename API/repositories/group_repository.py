from abc import ABC, abstractmethod
from typing import Optional, List

from sqlalchemy import select
from sqlalchemy.exc import NoResultFound
from sqlalchemy.orm import Session

from models.group import Group

class IGroupRepository(ABC):
    @abstractmethod
    def add(self, group: Group) -> Group: ...
    @abstractmethod
    def get_by_id(self, group_id: int) -> Optional[Group]: ...
    @abstractmethod
    def get_by_name(self, name: str) -> Optional[Group]: ...
    @abstractmethod
    def get_all(self, offset: int = 0, limit: int = 100) -> List[Group]: ...
    @abstractmethod
    def update(self, group_id: int, fields: dict) -> Group: ...
    @abstractmethod
    def delete(self, group_id: int) -> None: ...


class GroupRepository(IGroupRepository):
    def __init__(self, db: Session):
        self.db = db

    def add(self, group: Group) -> Group:
        self.db.add(group)
        self.db.commit()
        self.db.refresh(group)
        return group

    def get_by_id(self, group_id: int) -> Optional[Group]:
        stmt = select(Group).where(Group.id == group_id)
        return self.db.scalars(stmt).first()

    def get_by_name(self, name: str) -> Optional[Group]:
        stmt = select(Group).where(Group.name == name)
        return self.db.scalars(stmt).first()

    def get_all(self, offset: int = 0, limit: int = 100) -> List[Group]:
        stmt = (
            select(Group)
            .order_by(Group.id)
            .offset(offset)
            .limit(limit)
        )
        return list(self.db.scalars(stmt))

    def update(self, group_id: int, fields: dict) -> Group:
        group = self.get_by_id(group_id)
        if not group:
            raise NoResultFound(f"Group with id {group_id} not found.")

        for key, value in fields.items():
            if hasattr(group, key):
                setattr(group, key, value)

        self.db.commit()
        self.db.refresh(group)
        return group

    def delete(self, group_id: int) -> None:
        group = self.get_by_id(group_id)
        if not group:
            raise NoResultFound(f"Group with id {group_id} not found.")

        self.db.delete(group)
        self.db.commit()