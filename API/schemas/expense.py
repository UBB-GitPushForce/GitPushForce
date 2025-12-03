from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


class ExpenseBase(BaseModel):
    """
    Base model for the expense
    """
    title: str = Field(..., max_length=255)
    amount: float = Field(..., gt=0)
    category_id: int

    class Config:
        from_attributes = True


class ExpenseCreate(ExpenseBase):
    """
    DTO for creating
    """
    group_id: Optional[int] = Field(None, description="Group this expense belongs to")
    description: Optional[str] = None  


class ExpenseUpdate(BaseModel):
    """
    DTO for updating
    """
    title: Optional[str] = Field(None, max_length=255)
    amount: Optional[float] = Field(None, gt=0)
    description: Optional[str] = None 


class ExpenseResponse(ExpenseBase):
    """
    DTO for responses
    """
    id: int
    user_id: int
    group_id: Optional[int] = None
    created_at: datetime
    description: Optional[str] = None

    split_amount: float

    class Config:
        from_attributes = True

class Expense(ExpenseBase):
    id: int
    user_id: Optional[int] = None
    group_id: Optional[int] = None
    created_at: datetime
    
    split_amount: float 

    class Config:
        from_attributes = True
