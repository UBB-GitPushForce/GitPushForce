from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


class ExpenseBase(BaseModel):
    title: str = Field(..., max_length=255)
    category: str = Field(..., max_length=100)
    amount: float = Field(..., gt=0)

    class Config:
        from_attributes = True


class ExpenseCreate(ExpenseBase):
    group_id: int = Field(..., description="Group this expense belongs to")
    description: Optional[str] = None  


class ExpenseUpdate(BaseModel):
    title: Optional[str] = Field(None, max_length=255)
    category: Optional[str] = Field(None, max_length=100)
    amount: Optional[float] = Field(None, gt=0)
    description: Optional[str] = None 


class Expense(ExpenseBase):
    id: int
    user_id: int
    group_id: int
    created_at: datetime
    description: Optional[str] = None

    class Config:
        from_attributes = True
