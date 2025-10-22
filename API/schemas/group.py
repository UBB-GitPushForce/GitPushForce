from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime


class GroupBase(BaseModel):
    name: str = Field(..., min_length=1, max_length=255)
    description: Optional[str] = Field(None, max_length=255)

    class Config:
        from_attributes = True


class GroupCreate(GroupBase):
    pass


class GroupUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=255)
    description: Optional[str] = Field(None, max_length=255)


class Group(GroupBase):
    id: int
    created_at: datetime
