from sqlalchemy import Column, ForeignKey, Integer

from models.base import Base


class UsersGroups(Base):
    """
    Defines the UsersGroups association model used to link users with groups.

    Args:
        user_id (int) identifier of the linked user
        group_id (int) identifier of the linked group

    Returns:
        UsersGroups object representing one user�group link

    Exceptions:
        None
    """

    __tablename__ = "users_groups"

    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    group_id = Column(Integer, ForeignKey("groups.id", ondelete="CASCADE"), primary_key=True)
