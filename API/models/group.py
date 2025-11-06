from sqlalchemy import Column, DateTime, Integer, String, func
from sqlalchemy.orm import relationship

from models.base import Base


class Group(Base):
    __tablename__ = "groups"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    description = Column(String(255))
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    # Relația cu User (mulți-la-mulți)
    users = relationship(
        "User",
        secondary="users_groups",
        back_populates="groups"
    )

    # ✅ Relația cu Expense (unu-la-mulți)
    expenses = relationship(
        "Expense",
        back_populates="group",
        cascade="all, delete-orphan",
        passive_deletes=True
    )
