import io
from abc import ABC, abstractmethod
from typing import List

import qrcode
from models.group import Group
from repositories.group_repository import IGroupRepository
from schemas.group import GroupCreate, GroupUpdate
from sqlalchemy.exc import NoResultFound
from utils.helpers.generate_invitation_code import generate_invitation_code


class IGroupService(ABC):
    """
    Defines the interface for all group service operations.

    Args:
        data (GroupCreate) payload for group creation
        group_id (int) identifier of the group
        group_name (str) name of the group
        offset (int) number of items to skip
        limit (int) maximum number of items to return
        data (GroupUpdate) payload for updating group data

    Returns:
        Group or list[Group] depending on method

    Exceptions:
        NoResultFound raised when a group cannot be found
        ValueError raised when update data is invalid
    """

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
        """
        Initializes the group service with a repository.

        Args:
            repository (IGroupRepository) group repository instance

        Returns:
            None

        Exceptions:
            None
        """
        self.repository = repository

    def create_group(self, data: GroupCreate) -> Group:
        """
        Creates a new group.

        Args:
            data (GroupCreate) validated creation fields

        Returns:
            Group newly created group

        Exceptions:
            None
        """

        # Generate unique 6-character invitation code
        code = generate_invitation_code()
        while self.repository.get_by_invitation_code(code):
            code = generate_invitation_code()

        group = Group(
            name=data.name,
            description=data.description,
            invitation_code=code
        )

        return self.repository.add(group)

    def get_group_by_id(self, group_id: int) -> Group:
        """
        Retrieves a group by its id.

        Args:
            group_id (int) identifier of the group

        Returns:
            Group matching group

        Exceptions:
            NoResultFound raised when the group does not exist
        """
        group = self.repository.get_by_id(group_id)
        if not group:
            raise NoResultFound(f"Group with id {group_id} not found.")
        return group

    def get_group_by_name(self, group_name: str) -> Group:
        """
        Retrieves a group by its name.

        Args:
            group_name (str) name of the group

        Returns:
            Group matching the given name

        Exceptions:
            NoResultFound raised when the group does not exist
        """
        group = self.repository.get_by_name(group_name)
        if not group:
            raise NoResultFound(f"Group with name {group_name} not found.")
        return group

    def get_all_groups(self, offset: int = 0, limit: int = 100) -> List[Group]:
        """
        Retrieves all groups with pagination.

        Args:
            offset (int) items to skip
            limit (int) maximum items to return

        Returns:
            list[Group] paginated list of groups

        Exceptions:
            None
        """
        return self.repository.get_all(offset=offset, limit=limit)

    def update_group(self, group_id: int, data: GroupUpdate) -> Group:
        """
        Updates a group with the provided fields.

        Args:
            group_id (int) identifier of the group
            data (GroupUpdate) update payload

        Returns:
            Group updated group object

        Exceptions:
            NoResultFound raised when the group does not exist
            ValueError raised when no update fields are provided
        """
        self.get_group_by_id(group_id)
        fields = data.model_dump(exclude_unset=True)
        if not fields:
            raise ValueError("No fields provided for update.")
        return self.repository.update(group_id, fields)

    def delete_group(self, group_id: int) -> None:
        """
        Deletes a group.

        Args:
            group_id (int) identifier of the group

        Returns:
            None

        Exceptions:
            NoResultFound raised when the group does not exist
        """
        self.get_group_by_id(group_id)
        self.repository.delete(group_id)

    def generate_invite_qr(self, group_id: int) -> bytes:
        group = self.repository.get_by_id(group_id)
        if not group:
            raise NoResultFound(f"Group with id {group_id} not found.")
        qr = qrcode.QRCode(
            version=1,
            error_correction=qrcode.constants.ERROR_CORRECT_L,
            box_size=10,
            border=4,
        )
        qr.add_data(group.invitation_code)
        qr.make(fit=True)
        img = qr.make_image(fill_color="black", back_color="white")
        buffer = io.BytesIO()
        img.save(buffer, format="PNG")
        buffer.seek(0)
        return buffer.read()
