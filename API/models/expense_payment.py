from sqlalchemy import Column, DateTime, ForeignKey, Integer, PrimaryKeyConstraint, func

from models.base import Base


class ExpensePayment(Base):
    __tablename__ = "expense_payments"

    expense_id = Column(Integer, ForeignKey("expenses.id", ondelete="CASCADE"), nullable=False)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    paid_at = Column(DateTime(timezone=True), server_default=func.now())

    __table_args__ = (
        PrimaryKeyConstraint("expense_id", "user_id", name="pk_expense_payment"),
    )
