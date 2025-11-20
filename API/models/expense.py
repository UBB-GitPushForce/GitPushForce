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
    Provides the Expense model used to store a single expense record.

    Args:
        id (int) unique identifier for the expense
        user_id (int) links the expense to a user
        group_id (int) links the expense to a group
        title (str) short text for the expense name
        category (str) text label that groups similar expenses
        amount (float) numeric value of the expense
        description (str) long optional text describing the expense
        created_at (datetime) creation timestamp
        user (User) related user object
        group (Group) related group object

    Returns:
        Expense object representing one stored expense

    Exceptions:
        CheckConstraintError raised when user_id is null or rules are not respected
    """

    __tablename__ = "expenses"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=True)
    group_id = Column(Integer, ForeignKey("groups.id", ondelete="CASCADE"), nullable=True)
    title = Column(String(255))
    category = Column(String(100))
    amount = Column(Float, nullable=False)
    description = Column(Text, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="expenses", passive_deletes=True)
    group = relationship("Group", back_populates="expenses", passive_deletes=True)

    __table_args__ = (
        CheckConstraint(
            "((user_id IS NOT NULL AND group_id IS NULL) OR "
            "(user_id IS NOT NULL AND group_id IS NOT NULL))",
            name="chk_expenses_one_fk"
        ),
    )
