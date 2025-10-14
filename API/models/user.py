from sqlalchemy import (
    Column,
    Integer,
    String,
    DateTime,
    CheckConstraint,
    func
)
from sqlalchemy.orm import relationship
from .base import Base

class User(Base):
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

    expenses = relationship("Expense", back_populates="user", cascade="all, delete-orphan")

    groups = relationship(
        "Group",
        secondary="users_groups",
        back_populates="users"
    )