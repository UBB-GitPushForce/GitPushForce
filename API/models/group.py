from sqlalchemy import Column, DateTime, Integer, String, func
from sqlalchemy.orm import relationship

from models.base import Base


class Group(Base):
    """
    Defines the Group model used to store a single group record.

    Args:
        id (int) unique identifier for the group
        name (str) name of the group
        description (str) short text describing the group
        created_at (datetime) creation timestamp
        users (list[User]) users linked to this group
        expenses (list[Expense]) expenses linked to this group

    Returns:
        Group object representing one stored group

    Exceptions:
        None
    """

    __tablename__ = "groups"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    description = Column(String(255))
    invitation_code = Column(String(255), nullable=False, unique=True)  # <-- ADDED
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    users = relationship(
        "User",
        secondary="users_groups",
        back_populates="groups"
    )

    expenses = relationship(
        "Expense",
        back_populates="group",
        cascade="all, delete-orphan",
        passive_deletes=True
    )
