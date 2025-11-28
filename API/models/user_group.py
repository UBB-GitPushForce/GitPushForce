from sqlalchemy import Column, ForeignKey, Integer

from models.base import Base


class UserGroup(Base):
    """
    Model for UserGroup in the database.
    """

    __tablename__ = "users_groups"

    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    group_id = Column(Integer, ForeignKey("groups.id", ondelete="CASCADE"), primary_key=True)
