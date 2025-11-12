from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field, model_validator


class ExpenseBase(BaseModel):
    title: str = Field(..., max_length=255)
    category: str = Field(..., max_length=100)
    amount: float = Field(..., gt=0, description="Expense amount. Must be positive.")

    class Config:
        from_attributes = True


class ExpenseCreate(ExpenseBase):
    user_id: Optional[int] = None
    group_id: Optional[int] = None

    @model_validator(mode="after")
    def validate_one_fk(self):
        """Ensure exactly one of user_id or group_id is set."""
        if (self.user_id is None and self.group_id is None) or (
            self.user_id is not None and self.group_id is not None
        ):
            raise ValueError("Exactly one of user_id or group_id must be provided.")
        return self


class ExpenseUpdate(BaseModel):
    title: Optional[str] = Field(None, max_length=255)
    category: Optional[str] = Field(None, max_length=100)
    amount: Optional[float] = Field(None, gt=0)


class Expense(ExpenseBase):
    id: int
    user_id: Optional[int] = None
    group_id: Optional[int] = None
    created_at: datetime

    class Config:
        from_attributes = True
