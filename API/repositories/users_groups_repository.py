from abc import ABC, abstractmethod
from typing import List

from models.group import Group
from models.user import User
from models.users_groups import UsersGroups
from sqlalchemy import delete, select, func
from sqlalchemy.orm import Session


class IUsersGroupsRepository(ABC):
    # CREATE
    @abstractmethod
    def add_user_to_group(self, user_id: int, group_id: int) -> None: ...

    # READ
    @abstractmethod
    def get_groups_by_user(self, user_id: int) -> List[Group]: ...
    @abstractmethod
    def get_users_by_group(self, group_id: int) -> List[User]: ...
    @abstractmethod
    def get_nr_of_users_from_group(self, group_id: int) -> int: ...

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
