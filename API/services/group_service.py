from abc import ABC, abstractmethod
from typing import List

from models.group import Group
from repositories.group_repository import GroupRepository, IGroupRepository
from repositories.users_groups_repository import (
    IUsersGroupsRepository,
    UsersGroupsRepository,
)
from schemas.group import GroupCreate, GroupUpdate
from sqlalchemy.exc import NoResultFound


class IGroupService(ABC):
    @abstractmethod
    def create_group(self, name: str, description: str | None = None) -> Group: ...
    @abstractmethod
    def get_group(self, group_id: int) -> Group: ...
    @abstractmethod
    def get_all_groups(self, offset: int = 0, limit: int = 100) -> List[Group]: ...
    @abstractmethod
    def update_group(self, group_id: int, fields: dict) -> Group: ...
    @abstractmethod
    def delete_group(self, group_id: int) -> None: ...
    @abstractmethod
    def add_user_to_group(self, user_id: int, group_id: int): ...
    @abstractmethod
    def remove_user_from_group(self, user_id: int, group_id: int): ...
    @abstractmethod
    def get_groups_by_user(self, user_id: int) -> List[Group]: ...
    @abstractmethod
    def get_users_by_group(self, group_id: int) -> List: ...


class GroupService:
    def __init__(self, db_session):
        self.repository: IGroupRepository = GroupRepository(db_session)
        self.users_groups_repo: IUsersGroupsRepository = UsersGroupsRepository(db_session)

    def create_group(self, group_in: GroupCreate) -> Group:
        # prevent duplicate group names
        existing = self.repository.get_by_name(group_in.name)
        if existing:
            raise ValueError(f"Group with name '{group_in.name}' already exists.")

        group = Group(name=group_in.name, description=group_in.description)
        return self.repository.add(group)

    def get_group(self, group_id: int) -> Group:
        group = self.repository.get_by_id(group_id)
        if not group:
            raise NoResultFound(f"Group with id {group_id} not found.")
        return group

    def get_all_groups(self, offset: int = 0, limit: int = 100) -> List[Group]:
        return self.repository.get_all(offset=offset, limit=limit)

    def update_group(self, group_id: int, group_in: GroupUpdate) -> Group:
        # ensure group exists
        self.get_group(group_id)
        fields = group_in.model_dump(exclude_unset=True)
        if not fields:
            raise ValueError("No fields provided for update.")
        return self.repository.update(group_id, fields)

    def delete_group(self, group_id: int) -> None:
        # ensure group exists
        self.get_group(group_id)
        self.repository.delete(group_id)

    def add_user_to_group(self, user_id: int, group_id: int):
        # ensure group exists
        self.get_group(group_id)
        return self.users_groups_repo.add_user_to_group(user_id, group_id)

    def remove_user_from_group(self, user_id: int, group_id: int):
        # ensure group exists
        self.get_group(group_id)
        return self.users_groups_repo.remove_user_from_group(user_id, group_id)

    def get_groups_by_user(self, user_id: int) -> List[Group]:
        return self.users_groups_repo.get_groups_by_user(user_id)

    def get_users_by_group(self, group_id: int) -> List:
        # ensure group exists
        self.get_group(group_id)
        return self.users_groups_repo.get_users_by_group(group_id)
