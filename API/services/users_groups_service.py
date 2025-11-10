from abc import ABC, abstractmethod
from typing import List

from models import User
from models.group import Group
from repositories.users_groups_repository import IUsersGroupsRepository
from utils.helpers.logger import Logger


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
    logger = Logger()
    
    def __init__(self, repository: IUsersGroupsRepository):
        self.repository = repository

    def add_user_to_group(self, user_id: int, group_id: int) -> None:
        self.logger.debug(f"Adding user {user_id} to group {group_id}")
        self.repository.add_user_to_group(user_id, group_id)

    def get_users_from_group(self, group_id: int) -> List[User]:
        self.logger.debug(f"Getting users from group {group_id}")
        return self.repository.get_users_by_group(group_id)

    def get_user_groups(self, user_id: int) -> List[Group]:
        self.logger.debug(f"Getting groups for user {user_id}")
        return self.repository.get_groups_by_user(user_id)

    def delete_user_from_group(self, user_id: int, group_id: int) -> None:
        self.logger.debug(f"Removing user {user_id} from group {group_id}")
        self.repository.remove_user_from_group(user_id, group_id)
