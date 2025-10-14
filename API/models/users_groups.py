from sqlalchemy import Column, Integer, ForeignKey
from .base import Base

class UsersGroups(Base):
    __tablename__ = "users_groups"

    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    group_id = Column(Integer, ForeignKey("groups.id", ondelete="CASCADE"), primary_key=True)
