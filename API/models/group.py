from sqlalchemy import Column, Integer, String, DateTime, func
from sqlalchemy.orm import relationship
from .base import Base

class Group(Base):
    __tablename__ = "groups"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    description = Column(String(255))
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    users = relationship(
        "User",
        secondary="users_groups",
        back_populates="groups"
    )
