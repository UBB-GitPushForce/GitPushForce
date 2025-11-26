from datetime import datetime

from pydantic import BaseModel, Field


class ExpensePaymentBase(BaseModel):
    user_id: int = Field(..., description="User who paid")


class ExpensePaymentCreate(ExpensePaymentBase):
    pass


class ExpensePaymentResponse(ExpensePaymentBase):
    expense_id: int
    paid_at: datetime

    class Config:
        from_attributes = True
