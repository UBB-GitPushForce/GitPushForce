from pydantic import BaseModel, Field
from datetime import datetime


class ExpensePaymentBase(BaseModel):
    user_id: int = Field(..., description="User who paid")


class ExpensePaymentCreate(ExpensePaymentBase):
    pass


class ExpensePaymentResponse(ExpensePaymentBase):
    expense_id: int
    paid_at: datetime

    class Config:
        from_attributes = True
