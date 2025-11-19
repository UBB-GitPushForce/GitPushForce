from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


class GroupBase(BaseModel):
    """
    Defines the base fields shared by all group schemas.

    Args:
        name (str) group name
        description (str) optional group description

    Returns:
        GroupBase base model for shared fields

    Exceptions:
        None
    """
    name: str = Field(..., min_length=1, max_length=255)
    description: Optional[str] = Field(None, max_length=255)

    class Config:
        from_attributes = True


class GroupCreate(GroupBase):
    """
    Schema for creating a new group.

    Args:
        name (str) group name
        description (str) optional group description

    Returns:
        GroupCreate validated creation data

    Exceptions:
        None
    """
    # invitation_code is NOT included; backend generates it
    pass


class GroupUpdate(BaseModel):
    """
    Schema for updating an existing group.

    Args:
        name (str) updated group name
        description (str) updated description

    Returns:
        GroupUpdate validated update data

    Exceptions:
        None
    """
    name: Optional[str] = Field(None, min_length=1, max_length=255)
    description: Optional[str] = Field(None, max_length=255)


class Group(GroupBase):
    """
    Schema returned to clients representing a complete group record.

    Args:
        id (int) unique identifier of the group
        created_at (datetime) timestamp of group creation
        invitation_code (str) unique invitation code for joining the group

    Returns:
        Group API response model

    Exceptions:
        None
    """
    id: int
    created_at: datetime
    invitation_code: str  
