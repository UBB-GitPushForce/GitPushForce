from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime


class ExpenseBase(BaseModel):
    title: str = Field(..., max_length=255)
    category: str = Field(..., max_length=100)
    amount: float = Field(..., gt=0, description="Expense amount. Must be positive.")

    class Config:
        from_attributes = True 


class ExpenseCreate(ExpenseBase):
    pass


class ExpenseUpdate(BaseModel):
    title: Optional[str] = Field(None, max_length=255)
    category: Optional[str] = Field(None, max_length=100)
    amount: Optional[float] = Field(None, gt=0)


class Expense(ExpenseBase):
    id: int
    user_id: int
    created_at: datetime
