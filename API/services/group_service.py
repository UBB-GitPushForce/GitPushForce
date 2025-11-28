import io
from abc import ABC, abstractmethod

import qrcode
from fastapi import HTTPException
from models.group import Group
from repositories.group_repository import IGroupRepository
from schemas.api_response import APIResponse
from schemas.group import GroupCreate, GroupResponse, GroupUpdate
from utils.helpers.constants import STATUS_BAD_REQUEST, STATUS_NOT_FOUND
from utils.helpers.generate_invitation_code import generate_invitation_code


class IGroupService(ABC):
    """
    Interface for the group service. For achieving loose coupling.
    """

    @abstractmethod
    def create_group(self, data: GroupCreate) -> APIResponse: ...

    @abstractmethod
    def get_group_by_id(self, group_id: int) -> APIResponse: ...
    
    @abstractmethod
    def get_all_groups(self, offset: int = 0, limit: int = 100) -> APIResponse: ...

    @abstractmethod
    def update_group(self, group_id: int, data: GroupUpdate) -> APIResponse: ...

    @abstractmethod
    def delete_group(self, group_id: int) -> APIResponse: ...
    
    @abstractmethod
    def generate_invite_qr(self, group_id: int) -> APIResponse: ...


class GroupService(IGroupService):
    def __init__(self, repository: IGroupRepository):
        """
        Constructor method.
        """
        self.repository = repository

    def _validate_group(self, group_id: int):
        group = self.repository.get_by_id(group_id)
        if not group:
            raise HTTPException(status_code= STATUS_NOT_FOUND, detail=f"Group with id {group_id} not found.")
        
        return group

    def create_group(self, data: GroupCreate) -> APIResponse:
        """
        Method for creating a new group
        """
        code = generate_invitation_code()
        while self.repository.get_by_invitation_code(code):
            code = generate_invitation_code()

        group = Group(
            name=data.name,
            description=data.description,
            invitation_code=code
        )

        id = self.repository.add(group)
        
        return APIResponse(
            success=True,
            data=id,
        )

    def get_group_by_id(self, group_id: int) -> APIResponse:
        """
        Method for retrieving group by id.
        """
        self._validate_group(group_id)
        
        group = self.repository.get_by_id(group_id)
        
        group_response = GroupResponse.model_validate(group)
        
        return APIResponse(
            success=True,
            data=group_response,
        )

    def get_all_groups(self, offset: int = 0, limit: int = 100) -> APIResponse:
        """
        Method for retrieving all groups with pagination.
        """
        groups = self.repository.get_all(offset=offset, limit=limit)
        
        groups_response = [GroupResponse.model_validate(group) for group in groups]
        
        return APIResponse(
            success=True,
            data=groups_response
        )

    def update_group(self, group_id: int, data: GroupUpdate) -> APIResponse:
        """
        Method for updating a group with the provided fields.
        """
        self._validate_group(group_id)

        fields = data.model_dump(exclude_unset=True)
        if not fields:
            raise HTTPException(status_code=STATUS_BAD_REQUEST, detail="No fields provided for update.")
        
        id = self.repository.update(group_id, fields)
        
        return APIResponse(
            success=True,
            data=id,
        )

    def delete_group(self, group_id: int) -> APIResponse:
        """
        Method for deleting a group.
        """
        self._validate_group(group_id)
        
        id = self.repository.delete(group_id)
        
        return APIResponse(
            success=True,
            data=id,
        )

    def generate_invite_qr(self, group_id: int) -> APIResponse:
        """
        Method for generating qr code for group invite.
        """
        group = self._validate_group(group_id)
        
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
        
        return APIResponse(
            success=True,
            data=buffer.read(),
        )
