from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, func, CheckConstraint
from models.base import Base


class GroupLog(Base):
    __tablename__ = "group_logs"

    id = Column(Integer, primary_key=True)
    group_id = Column(Integer, ForeignKey("groups.id", ondelete="CASCADE"), nullable=False)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    action = Column(String(20), nullable=False)  # JOIN, LEAVE
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    __table_args__ = (
        CheckConstraint("action IN ('JOIN', 'LEAVE')", name="chk_group_log_action"),
    )
