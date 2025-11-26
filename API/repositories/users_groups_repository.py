from abc import ABC, abstractmethod
from sqlite3 import IntegrityError
from typing import List

from models.group import Group
from models.user import User
from models.users_groups import UsersGroups
from sqlalchemy import delete, func, select
from sqlalchemy.orm import Session


class IUsersGroupsRepository(ABC):
    # CREATE
    @abstractmethod
    def add_user_to_group(self, user_id: int, group_id: int) -> None: ...
    @abstractmethod
    def add_user_to_group_by_invitation_code(self, user_id: int, invitation_code: str) -> None: ...

    # READ
    @abstractmethod
    def get_groups_by_user(self, user_id: int) -> List[Group]: ...
    @abstractmethod
    def get_users_by_group(self, group_id: int) -> List[User]: ...
    @abstractmethod
    def get_nr_of_users_from_group(self, group_id: int) -> int: ...
    @abstractmethod
    def is_member(self, user_id: int, group_id: int) -> bool: ...

    # UPDATE (No, wtf do you want to update??)

    # DELETE
    @abstractmethod
    def remove_user_from_group(self, user_id: int, group_id: int) -> None: ...


class UsersGroupsRepository(IUsersGroupsRepository):
    def __init__(self, db: Session):
        self.db = db

    def add_user_to_group(self, user_id: int, group_id: int) -> None:
        """
        Links a user to a group.

        Args:
            user_id (int) id of the user
            group_id (int) id of the group

        Returns:
            None no return value

        Exceptions:
            None
        """

        users_groups = UsersGroups(user_id=user_id, group_id=group_id)
        self.db.add(users_groups)
        self.db.commit()
        self.db.refresh(users_groups)

    def add_user_to_group_by_invitation_code(self, user_id: int, invitation_code: str) -> None:
        # 1. Find group by invitation code
        stmt = select(Group).where(Group.invitation_code == invitation_code)

        group = self.db.scalar(stmt)
        if not group:
            raise ValueError("Invalid invitation code")

        # 2. Create relationship (if not already exists)
        try:
            users_groups = UsersGroups(user_id=user_id, group_id=group.id)
            self.db.add(users_groups)
            self.db.commit()
            self.db.refresh(users_groups)
        except IntegrityError:
            self.db.rollback()
            raise IntegrityError("User is already in this group", None, None)

    def remove_user_from_group(self, user_id: int, group_id: int) -> None:
        """
        Removes a user from a group.

        Args:
            user_id (int) id of the user
            group_id (int) id of the group

        Returns:
            None no return value

        Exceptions:
            None
        """

        stmt = (
            delete(UsersGroups)
            .where(UsersGroups.user_id == user_id and UsersGroups.group_id == group_id)
        )
        self.db.execute(stmt)
        self.db.commit()

    def get_groups_by_user(self, user_id: int) -> List[Group]:
        """
        Retrieves all groups linked to a user.

        Args:
            user_id (int) id of the user

        Returns:
            list[Group] groups the user belongs to

        Exceptions:
            None
        """

        stmt = (
            select(Group)
            .join(UsersGroups, UsersGroups.group_id == Group.id)
            .where(UsersGroups.user_id == user_id)
        )
        return list(self.db.scalars(stmt))

    def get_users_by_group(self, group_id: int) -> List[User]:
        """
        Retrieves all users linked to a group.

        Args:
            group_id (int) id of the group

        Returns:
            list[User] users belonging to the group

        Exceptions:
            None
        """

        stmt = (
            select(User)
            .join(UsersGroups, UsersGroups.user_id == User.id)
            .where(UsersGroups.group_id == group_id)
        )
        return list(self.db.scalars(stmt))

    def get_nr_of_users_from_group(self, group_id: int) -> int:
        stmt = (
            select(func.count(UsersGroups.user_id))
            .where(UsersGroups.group_id == group_id)
        )

        return self.db.scalar(stmt)
    
    def is_member(self, user_id: int, group_id: int) -> bool:
        return (
            self.db.query(UsersGroups)
            .filter_by(user_id=user_id, group_id=group_id)
            .first()
            is not None
        )
