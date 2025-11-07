from abc import ABC, abstractmethod
from typing import List

from models import User
from models.group import Group
from repositories.users_groups_repository import IUsersGroupsRepository


class IUsersGroupsService(ABC):
    # CREATE
    @abstractmethod
    def add_user_to_group(self, user_id: int, group_id: int) -> None: ...

    # READ
    @abstractmethod
    def get_users_from_group(self, group_id: int) -> List[User]: ...
    @abstractmethod
    def get_user_groups(self, user_id: int) -> List[Group]: ...

    # DELETE
    @abstractmethod
    def delete_user_from_group(self, user_id: int, group_id: int) -> None: ...


class UsersGroupsService(IUsersGroupsService):
    def __init__(self, repository: IUsersGroupsRepository):
        self.repository = repository

    def add_user_to_group(self, user_id: int, group_id: int) -> None:
        self.repository.add_user_to_group(user_id, group_id)

    def get_users_from_group(self, group_id: int) -> List[User]:
        return self.repository.get_users_by_group(group_id)

    def get_user_groups(self, user_id: int) -> List[Group]:
        return self.repository.get_groups_by_user(user_id)

    def delete_user_from_group(self, user_id: int, group_id: int) -> None:
        self.repository.remove_user_from_group(user_id, group_id)
