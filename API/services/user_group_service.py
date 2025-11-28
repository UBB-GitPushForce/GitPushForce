from abc import ABC, abstractmethod

from fastapi import HTTPException
from models.group import Group
from models.user import User
from models.user_group import UserGroup
from repositories.group_repository import IGroupRepository
from repositories.user_group_repository import IUserGroupRepository
from repositories.user_repository import IUserRepository
from schemas.api_response import APIResponse
from schemas.group import GroupResponse
from schemas.user import UserResponse
from utils.helpers.constants import (
    GROUP_FIELD,
    STATUS_BAD_REQUEST,
    STATUS_INTERNAL_SERVER_ERROR,
    STATUS_NOT_FOUND,
    USER_FIELD,
)
from utils.helpers.logger import Logger


class IUserGroupService(ABC):
    """
    Interface for the UserGroup service. Achieving loose coupling.
    """
    @abstractmethod
    def add_user_to_group(self, user_id: int, group_id: int) -> APIResponse: ...
    
    @abstractmethod
    def add_user_to_group_by_invitation_code(self, user_id: int, invitation_code: str) -> APIResponse: ...
    
    @abstractmethod
    def get_users_from_group(self, group_id: int) -> APIResponse: ...
    
    @abstractmethod
    def get_user_groups(self, user_id: int) -> APIResponse: ...
    
    @abstractmethod
    def get_nr_of_users_from_group(self, group_id: int) -> APIResponse: ...

    @abstractmethod
    def delete_user_from_group(self, user_id: int, group_id: int) -> APIResponse: ...


class UserGroupService(IUserGroupService):
    """
    Implementation of IUserGroupService
    """
    
    def __init__(
        self,
        repository: IUserGroupRepository,
        group_repo: IGroupRepository,
        user_repo: IUserRepository,
    ):
        """
        Constructor method.
        """
        self.repository = repository
        self.group_repo = group_repo
        self.user_repo = user_repo
        self.logger = Logger()
        
    def _validate_group(self, group_id: int = None, invitation_code: str = None) -> Group:
        if group_id is not None:
            group = self.group_repo.get_by_id(group_id)
        elif invitation_code is not None:
            group = self.group_repo.get_by_invitation_code(invitation_code) 
        else:
            raise HTTPException(status_code=STATUS_INTERNAL_SERVER_ERROR, detail="Group could not be validated.")
          
        if not group:
            raise HTTPException(status_code=STATUS_NOT_FOUND, detail="Group not found.")
            
        return group
    
    def _validate_user(self, user_id: int) -> User:
        user = self.user_repo.get_by_id(user_id)
        if not user:
            raise HTTPException(status_code=STATUS_NOT_FOUND, detail="User not found")
        
        return user

    def add_user_to_group(self, user_id: int, group_id: int) -> APIResponse:
        """
        Method for adding a user to a group.
        """
        self.logger.debug(f"Adding user {user_id} to group {group_id}")

        self._validate_group(group_id)
        self._validate_user(user_id)
        
        already_in_group = self.repository.is_member(user_id, group_id)
        if already_in_group is True:
            return APIResponse(
                success=False,
                message="User is already in this group"
            )
        
        response = self.repository.add_user_to_group(UserGroup(user_id=user_id, group_id=group_id))
        
        group_id = response[0]
        user_id = response[1]
        
        return APIResponse(
            success=True,
            data={
                GROUP_FIELD: group_id,
                USER_FIELD: user_id,
            }
        )

    def add_user_to_group_by_invitation_code(self, user_id: int, invitation_code: str) -> APIResponse:
        """
        Method for adding a user to a group by invitation code
        """
        self.logger.info(f"Adding user with id {user_id} to group which has invitation code {invitation_code}")
        
        self._validate_user(user_id)
        group = self._validate_group(invitation_code=invitation_code)
        
        already_in_group = self.repository.is_member(user_id, group.id)
        if already_in_group is True:
            return APIResponse(
                success=False,
                message="User is already in this group"
            )
        
        response = self.repository.add_user_to_group_by_invitation_code(user_id, invitation_code)
        
        group_response = GroupResponse.model_validate(response[0])
        user_response = UserResponse.model_validate(response[1])
        
        return APIResponse(
            success=True,
            data={
                GROUP_FIELD: group_response,
                USER_FIELD: user_response,
            }
        )

    def get_users_from_group(self, group_id: int) -> APIResponse:
        """
        Method for retrieving users from a certain group.
        """
        self.logger.info(f"Getting users from group {group_id}")
        
        self._validate_group(group_id=group_id)
        
        users = self.repository.get_users_by_group(group_id)
        
        users_response = [UserResponse.model_validate(user) for user in users]
        
        return APIResponse(
            success=True,
            data=users_response
        )

    def get_user_groups(self, user_id: int) -> APIResponse:
        """
        Method for retrieving all groups a user belongs to.
        """
        self.logger.info(f"Getting groups for user {user_id}")
        
        self._validate_user(user_id=user_id)
        
        groups = self.repository.get_groups_by_user(user_id)
        
        groups_response = [GroupResponse.model_validate(group) for group in groups]
        
        return APIResponse(
            success=True,
            data=groups_response
        )

    def get_nr_of_users_from_group(self, group_id: int) -> APIResponse:
        """
        Method for retrieving number of users from a group.
        """
        self._validate_group(group_id=group_id)
        
        number = self.repository.get_nr_of_users_from_group(group_id)
        
        return APIResponse(
            success=True,
            data=number
        )

    def delete_user_from_group(self, user_id: int, group_id: int) -> APIResponse:
        """
        Method for removing a user from a group.
        """
        self.logger.info(f"Removing user {user_id} from group {group_id}")
        
        self._validate_group(group_id=group_id)
        
        already_in_group = self.repository.is_member(user_id, group_id)
        if already_in_group is False:
            self.logger.info(f"User with id {user_id} is not part of group with id {group_id}")
            raise HTTPException(status_code=STATUS_BAD_REQUEST, detail=f"User with id {user_id} is not part of group with id {group_id}")
        
        self.repository.remove_user_from_group(UserGroup(user_id=user_id, group_id=group_id))
        
        return APIResponse(
            success=True,
            message="Deleted user successfully."
        )
