from sqlalchemy import CheckConstraint, Column, DateTime, Integer, String, func
from sqlalchemy.orm import relationship

from models.base import Base


class User(Base):
    """
    Defines the User model used to store a single user record.

    Args:
        id (int) unique identifier for the user
        first_name (str) user first name
        last_name (str) user last name
        email (str) user email address
        hashed_password (str) user password hash
        phone_number (str) user phone number
        created_at (datetime) creation timestamp
        updated_at (datetime) update timestamp
        expenses (list[Expense]) expenses linked to this user
        groups (list[Group]) groups this user is part of

    Returns:
        User object representing one stored user

    Exceptions:
        CheckConstraintError raised when phone_number contains nonnumeric characters
    """

    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    first_name = Column(String(255), nullable=False)
    last_name = Column(String(255), nullable=False)
    email = Column(String(255), unique=True, nullable=False)
    hashed_password = Column(String(255), nullable=False)
    phone_number = Column(String(50), nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    __table_args__ = (
        CheckConstraint("phone_number ~ '^[0-9]+$'", name="ck_phone_number"),
    )

    expenses = relationship(
        "Expense",
        back_populates="user",
        cascade="all, delete-orphan"
    )

    groups = relationship(
        "Group",
        secondary="users_groups",
        back_populates="users"
    )