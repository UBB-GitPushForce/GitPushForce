from abc import ABC, abstractmethod
from typing import List, Optional

from models.group import Group
from sqlalchemy import select
from sqlalchemy.orm import Session


class IGroupRepository(ABC):
    """
    Interface for the group repository. Achieves loose coupling
    """
    @abstractmethod
    def add(self, group: Group) -> int: ...

    @abstractmethod
    def get_by_id(self, group_id: int) -> Optional[Group]: ...

    @abstractmethod
    def get_by_invitation_code(self, code: str) -> Optional[Group]: ...  

    @abstractmethod
    def get_all(self, offset: int = 0, limit: int = 100) -> List[Group]: ...

    @abstractmethod
    def update(self, group_id: int, fields: dict) -> int: ...

    @abstractmethod
    def delete(self, group_id: int) -> int: ...


class GroupRepository(IGroupRepository):
    def __init__(self, db: Session):
        self.db = db

    def add(self, group: Group) -> int:
        """
        Method for creating a new group.
        """
        self.db.add(group)
        self.db.commit()
        self.db.refresh(group)
        
        return group.id

    def get_by_id(self, group_id: int) -> Optional[Group]:
        """
        Method for retrieving group by id
        """
        statement = select(Group).where(Group.id == group_id)
        
        return self.db.scalars(statement).first()

    def get_by_invitation_code(self, code: str) -> Optional[Group]:
        """
        Retrieves one group by invitation code.
        """
        statement = select(Group).where(Group.invitation_code == code)
        
        return self.db.scalars(statement).first()

    def get_all(self, offset: int = 0, limit: int = 100) -> List[Group]:
        """
        Method for retrieving all groups with pagination.
        """
        statement = select(Group).order_by(Group.id).offset(offset).limit(limit)
        
        return list(self.db.scalars(statement))

    def update(self, group_id: int, fields: dict) -> int:
        """
        Method for updating a group.
        """
        group = self.get_by_id(group_id)
        for key, value in fields.items():
            if hasattr(group, key):
                setattr(group, key, value)
                
        self.db.commit()
        self.db.refresh(group)
        
        return group

    def delete(self, group_id: int) -> int:
        """
        Method for deleting group by id.
        """
        group = self.get_by_id(group_id)
        
        self.db.delete(group)
        self.db.commit()
        
        return group_id
