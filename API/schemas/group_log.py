from datetime import datetime

from pydantic import BaseModel


class GroupLogBase(BaseModel):
    group_id: int
    user_id: int
    action: str  # 'JOIN' or 'LEAVE'


class GroupLogResponse(GroupLogBase):
    id: int
    created_at: datetime

    class Config:
        from_attributes = True
