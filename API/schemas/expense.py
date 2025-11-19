from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field, model_validator


class ExpenseBase(BaseModel):
    """
    Defines the base fields shared by all expense schemas.

    Args:
        title (str) name of the expense
        category (str) category label of the expense
        amount (float) positive amount of the expense

    Returns:
        ExpenseBase base model for shared fields

    Exceptions:
        None
    """
    title: str = Field(..., max_length=255)
    category: str = Field(..., max_length=100)
    amount: float = Field(..., gt=0, description="Expense amount. Must be positive.")

    class Config:
        from_attributes = True


class ExpenseCreate(ExpenseBase):
    """
    Schema for creating a new expense.

    Args:
        user_id (int) id of the user creating the expense
        group_id (int) id of the group assigned to the expense

    Returns:
        ExpenseCreate validated expense creation data

    Exceptions:
        ValueError raised when user_id is missing
    """
    user_id: Optional[int] = None
    group_id: Optional[int] = None

    @model_validator(mode="after")
    def validate_one_fk(self):
        if self.user_id is None:
            raise ValueError("user_id must be provided.")

        return self


class ExpenseUpdate(BaseModel):
    """
    Schema for updating an existing expense.

    Args:
        title (str) updated title
        category (str) updated category
        amount (float) updated positive amount
        user_id (int) updated user id
        group_id (int) updated group id

    Returns:
        ExpenseUpdate validated update data

    Exceptions:
        ValueError raised when user_id is missing
    """
    title: Optional[str] = Field(None, max_length=255)
    category: Optional[str] = Field(None, max_length=100)
    amount: Optional[float] = Field(None, gt=0)
    user_id: Optional[int] = None
    group_id: Optional[int] = None

    @model_validator(mode="after")
    def validate_one_fk(self):
        if self.user_id is None:
            raise ValueError("user_id must be provided.")

        return self


class Expense(ExpenseBase):
    """
    Schema returned to clients representing a complete expense record.

    Args:
        id (int) unique identifier of the expense
        user_id (int) id of the user who owns the expense
        group_id (int) id of the related group
        created_at (datetime) timestamp when the expense was created

    Returns:
        Expense API response model

    Exceptions:
        None
    """
    id: int
    user_id: Optional[int] = None
    group_id: Optional[int] = None
    created_at: datetime

    class Config:
        from_attributes = True
