from sqlalchemy import CheckConstraint, Column, DateTime, Integer, String, Float, func
from sqlalchemy.orm import relationship

from models.base import Base


class User(Base):
    """
    User model. This is how the user is represented in the database schema.
    """
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    first_name = Column(String(255), nullable=False)
    last_name = Column(String(255), nullable=False)
    email = Column(String(255), unique=True, nullable=False)
    hashed_password = Column(String(255), nullable=False)
    phone_number = Column(String(50), nullable=False)
    budget = Column(Float, server_default=0, nullable=True) 

    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    __table_args__ = (
        CheckConstraint("phone_number ~ '^[0-9]+$'", name="ck_phone_number"),
        CheckConstraint("email ~ '^[^\s@]+@[^\s@]+\.[^\s@]+$'", name="ck_email"),
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

    categories = relationship(
        "Category",
        back_populates="user",
        cascade="all, delete-orphan"
    )
