from pydantic import BaseModel, Field
from typing import Optional


class ExpenseBase(BaseModel):
    # The amount must be provided and must be greater than zero.
    amount: float = Field(..., gt=0, description="The expense amount. Must be positive.")

    class Config:
        # Allows Pydantic to read ORM objects (SQLAlchemy models)
        from_attributes = True


class ExpenseCreate(ExpenseBase):
    pass


class ExpenseUpdate(BaseModel):
    # Amount is optional for updates, but if provided, must still be positive.
    amount: Optional[float] = Field(None, gt=0, description="The new expense amount. Must be positive if provided.")