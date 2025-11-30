from sqlalchemy import (
    Column,
    ForeignKey,
    Integer,
    String,
)
from sqlalchemy.dialects.postgresql import ARRAY
from sqlalchemy.orm import relationship

from models.base import Base


class Category(Base):
    __tablename__ = "categories"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=True)
    title = Column(String(30), nullable=False)
    keywords = Column(ARRAY(String))

    user = relationship("User", back_populates="categories", passive_deletes=True)
    expenses = relationship("Expense", back_populates="category", passive_deletes=True)