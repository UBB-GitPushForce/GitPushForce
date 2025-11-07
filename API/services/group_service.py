from abc import ABC, abstractmethod
from typing import List

from models.group import Group
from repositories.group_repository import IGroupRepository
from schemas.group import GroupCreate, GroupUpdate
from sqlalchemy.exc import NoResultFound


class IGroupService(ABC):
    # CREATE
    @abstractmethod
    def create_group(self, data: GroupCreate) -> Group: ...

    # READ
    @abstractmethod
    def get_group_by_id(self, group_id: int) -> Group: ...
    @abstractmethod
    def get_group_by_name(self, group_name: str) -> Group: ...
    @abstractmethod
    def get_all_groups(self, offset: int = 0, limit: int = 100) -> List[Group]: ...

    # UPDATE
    @abstractmethod
    def update_group(self, group_id: int, data: GroupUpdate) -> Group: ...

    # DELETE
    @abstractmethod
    def delete_group(self, group_id: int) -> None: ...


class GroupService(IGroupService):
    def __init__(self, repository: IGroupRepository):
        self.repository = repository

    def create_group(self, data: GroupCreate) -> Group:
        group = Group(name=data.name, description=data.description)
        return self.repository.add(group)

    def get_group_by_id(self, group_id: int) -> Group:
        group = self.repository.get_by_id(group_id)
        if not group:
            raise NoResultFound(f"Group with id {group_id} not found.")
        return group

    def get_group_by_name(self, group_name: str) -> Group:
        group = self.repository.get_by_name(group_name)
        if not group:
            raise NoResultFound(f"Group with name {group_name} not found.")
        return group

    def get_all_groups(self, offset: int = 0, limit: int = 100) -> List[Group]:
        return self.repository.get_all(offset=offset, limit=limit)

    def update_group(self, group_id: int, data: GroupUpdate) -> Group:
        # ensure group exists
        self.get_group_by_id(group_id)
        fields = data.model_dump(exclude_unset=True)
        if not fields:
            raise ValueError("No fields provided for update.")
        return self.repository.update(group_id, fields)

    def delete_group(self, group_id: int) -> None:
        # ensure group exists
        self.get_group_by_id(group_id)
        self.repository.delete(group_id)


