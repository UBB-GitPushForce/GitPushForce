from pydantic import BaseModel


class UserGroupBase(BaseModel):
    """
    DTO for UserGroup, used in API calls.
    """

    user_id: int
    group_id: int

class UserGroupCreate(UserGroupBase):
    """
    DTO for creation
    """
    pass

class UserGroupResponse(UserGroupBase):
    """
    DTO for responses in API
    """
    pass