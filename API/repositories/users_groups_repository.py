from abc import ABC, abstractmethod
from typing import List

from models.group import Group
from models.user import User
from models.users_groups import UsersGroups
from sqlalchemy import delete, select
from sqlalchemy.exc import NoResultFound
from sqlalchemy.orm import Session


class IUsersGroupsRepository(ABC):
    @abstractmethod
    def add_user_to_group(self, user_id: int, group_id: int) -> UsersGroups: ...
    @abstractmethod
    def remove_user_from_group(self, user_id: int, group_id: int) -> None: ...
    @abstractmethod
    def is_member(self, user_id: int, group_id: int) -> bool: ...
    @abstractmethod
    def get_groups_by_user(self, user_id: int) -> List[Group]: ...
    @abstractmethod
    def get_users_by_group(self, group_id: int) -> List[User]: ...


class UsersGroupsRepository(IUsersGroupsRepository):
    def __init__(self, db: Session):
        self.db = db

    def add_user_to_group(self, user_id: int, group_id: int) -> UsersGroups:
        # Check if already a member
        if self.is_member(user_id, group_id):
            stmt = select(UsersGroups).where(
                UsersGroups.user_id == user_id,
                UsersGroups.group_id == group_id,
            )
            existing = self.db.scalars(stmt).first()
            return existing

        users_groups = UsersGroups(user_id=user_id, group_id=group_id)
        self.db.add(users_groups)
        self.db.commit()
        self.db.refresh(users_groups)
        return users_groups

    def remove_user_from_group(self, user_id: int, group_id: int) -> None:
        stmt = (
            delete(UsersGroups)
            .where(UsersGroups.user_id == user_id, UsersGroups.group_id == group_id)
        )
        result = self.db.execute(stmt)
        if result.rowcount == 0:
            raise NoResultFound(
                f"No relationship found for user_id={user_id}, group_id={group_id}."
            )
        self.db.commit()

    def is_member(self, user_id: int, group_id: int) -> bool:
        stmt = select(UsersGroups).where(
            UsersGroups.user_id == user_id,
            UsersGroups.group_id == group_id,
        )
        return self.db.scalars(stmt).first() is not None

    def get_groups_by_user(self, user_id: int) -> List[Group]:
        stmt = (
            select(Group)
            .join(UsersGroups, UsersGroups.group_id == Group.id)
            .where(UsersGroups.user_id == user_id)
        )
        return list(self.db.scalars(stmt))

    def get_users_by_group(self, group_id: int) -> List[User]:
        stmt = (
            select(User)
            .join(UsersGroups, UsersGroups.user_id == User.id)
            .where(UsersGroups.group_id == group_id)
        )
        return list(self.db.scalars(stmt))