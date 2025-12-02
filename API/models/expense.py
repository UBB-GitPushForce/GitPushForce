from sqlalchemy import (
    CheckConstraint,
    Column,
    DateTime,
    Float,
    ForeignKey,
    Integer,
    String,
    Text,
    func,
)
from sqlalchemy.orm import relationship

from models.base import Base


class Expense(Base):
    """
    Database model for the expense
    """

    __tablename__ = "expenses"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=True)
    group_id = Column(Integer, ForeignKey("groups.id", ondelete="CASCADE"), nullable=True)
    title = Column(String(255))
    amount = Column(Float, nullable=False)
    description = Column(Text, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    category_id = Column(Integer, ForeignKey("categories.id", ondelete="CASCADE"), nullable=False)

    user = relationship("User", back_populates="expenses", passive_deletes=True)
    group = relationship("Group", back_populates="expenses", passive_deletes=True)
    category = relationship("Category", back_populates="expenses", passive_deletes=True)

    __table_args__ = (
        CheckConstraint(
            "((user_id IS NOT NULL AND group_id IS NULL) OR "
            "(user_id IS NOT NULL AND group_id IS NOT NULL))",
            name="chk_expenses_one_fk"
        ),
    )
