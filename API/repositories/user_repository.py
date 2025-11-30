from abc import ABC, abstractmethod
from datetime import datetime
from typing import List, Optional

from models.expense import Expense
from models.user import User
from sqlalchemy import func, select
from sqlalchemy.orm import Session


class IUserRepository(ABC):
    """
    Interface for the user repository. We defined this in order to achieve loose coupling. (Service will depend on this interface, not on any implementation of this interface)
    """
    @abstractmethod
    def add(self, user: User) -> int: ...
    
    @abstractmethod
    def get_by_id(self, user_id: int) -> Optional[User]: ...
    
    @abstractmethod
    def get_by_email(self, email: str) -> Optional[User]: ...
    
    @abstractmethod
    def get_all(self, offset: int = 0, limit: int = 100) -> List[User]: ...
    
    @abstractmethod
    def update(self, user_id: int, fields: dict) -> None: ...
    
    @abstractmethod
    def delete(self, user_id: int) -> None: ...


class UserRepository(IUserRepository):
    """
    Implementation for the IUserRepository interface.
    """
    def __init__(self, db: Session):
        self.db = db

    def add(self, user: User) -> int:
        """
        Method for creating an user. 
        """
        self.db.add(user)
        self.db.commit()
        self.db.refresh(user)
        
        return user.id

    def get_by_id(self, user_id: int) -> Optional[User]:
        """
        Method for retrieving an user by id.
        """
        statement = select(User).where(User.id == user_id)

        return self.db.scalars(statement).first()

    def get_by_email(self, email: str) -> Optional[User]:
        """
        Method for retrieving an user by email.
        """
        statement = select(User).where(User.email == email)
        
        return self.db.scalars(statement).first()

    def get_all(self, offset: int = 0, limit: int = 100) -> List[User]:
        """
        Method for retrieving all users. Supports offset and limit attributes.
        """
        statement = select(User).order_by(User.id).offset(offset).limit(limit)
        
        return list(self.db.scalars(statement))

    def update(self, user_id: int, fields: dict) -> None:
        """
        Method for updating a certain user. Only updates the fields provided with their new values.
        """
        user = self.get_by_id(user_id)
        for key, value in fields.items():
            if hasattr(user, key):
                setattr(user, key, value)
                      
        self.db.commit()
        self.db.refresh(user)
        return user

    def delete(self, user_id: int) -> None:
        """
        Method for deleting an user.
        """
        user = self.get_by_id(user_id)
        
        self.db.delete(user)
        self.db.commit()

    def get_user_monthly_spent(self, user_id: int, month_start: datetime) -> float:
        statement = (
            select(func.coalesce(func.sum(Expense.amount), 0.0))
            .where(Expense.user_id == user_id)
            .where(Expense.created_at >= month_start)
        )

        return self.db.scalar(statement)
