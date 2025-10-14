from sqlalchemy import (
    Column,
    Integer,
    Float,
    DateTime,
    ForeignKey,
    func
)
from sqlalchemy.orm import relationship
from .base import Base


class Expense(Base):
    __tablename__ = "expenses"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    amount = Column(Float, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="expenses")
