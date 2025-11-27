from abc import ABC, abstractmethod
from typing import List, Tuple

from models.group import Group
from models.user import User
from models.user_group import UserGroup
from sqlalchemy import delete, func, select
from sqlalchemy.orm import Session


class IUserGroupRepository(ABC):
    """
    Interface for UserGroupRepository, useful for loosely coupling the components.
    """
    @abstractmethod
    def get_group_by_invitation_code(self, invitation_code: str) -> Group | None: ...
    
    @abstractmethod
    def add_user_to_group(self, user_group: UserGroup) -> Tuple[int, int]: ...
    
    @abstractmethod
    def add_user_to_group_by_invitation_code(self, user_id: int, invitation_code: str) -> Tuple[int, int]: ...
    
    @abstractmethod
    def remove_user_from_group(self, user_group: UserGroup) -> None: ...
    
    @abstractmethod
    def get_groups_by_user(self, user_id: int) -> List[Group]: ...
    
    @abstractmethod
    def get_users_by_group(self, group_id: int) -> List[User]: ...
    
    @abstractmethod
    def get_nr_of_users_from_group(self, group_id: int) -> int: ...
    
    @abstractmethod
    def is_member(self, user_id: int, group_id: int) -> bool: ...


class UserGroupRepository(IUserGroupRepository):
    def __init__(self, db: Session):
        self.db = db
        
    def get_group_by_invitation_code(self, invitation_code: str) -> Group | None:
        """
        Method for retrieving a group id by invitation code.
        """
        statement = select(Group).where(Group.invitation_code == invitation_code)
        group = self.db.scalar(statement)
        
        return group

    def add_user_to_group(self, user_group: UserGroup) -> tuple:
        """
        Method for adding a user to a group.
        """
        self.db.add(user_group)
        self.db.commit()
        self.db.refresh(user_group)
        
        return (user_group.group_id, user_group.user_id)

    def add_user_to_group_by_invitation_code(self, user_id: int, invitation_code: str) -> tuple:
        """
        Method for adding a user to group by invitation code.
        """
        statement = select(Group).where(Group.invitation_code == invitation_code)
        group = self.db.scalar(statement)

        user_group = UserGroup(user_id=user_id, group_id=group.id)
            
        self.db.add(user_group)
        self.db.commit()
        self.db.refresh(user_group)
        
        return (group.id, user_id)

    def remove_user_from_group(self, user_group: UserGroup) -> None:
        """
        Method for removing a user from a group.
        """
        statement = delete(UserGroup).where((UserGroup.user_id == user_group.user_id) & (UserGroup.group_id == user_group.group_id))
        self.db.execute(statement)
        self.db.commit()

    def get_groups_by_user(self, user_id: int) -> List[Group]:
        """
        Method for returning groups in which a certain user belongs to.
        """
        statement = select(Group).join(UserGroup, UserGroup.group_id == Group.id).where(UserGroup.user_id == user_id)
        
        return list(self.db.scalars(statement))

    def get_users_by_group(self, group_id: int) -> List[User]:
        """
        Method for retrieving all users linked to a group.
        """
        statement = select(User).join(UserGroup, UserGroup.user_id == User.id).where(UserGroup.group_id == group_id)

        return list(self.db.scalars(statement))

    def get_nr_of_users_from_group(self, group_id: int) -> int:
        """
        Method for returning number of users from a certain group.
        """
        statement = select(func.count(UserGroup.user_id)).where(UserGroup.group_id == group_id)

        return self.db.scalar(statement)
    
    def is_member(self, user_id: int, group_id: int) -> bool:
        """
        Method for verifying if a user belongs to a group.
        """
        return self.db.query(UserGroup).filter_by(user_id=user_id, group_id=group_id).first() is not None


