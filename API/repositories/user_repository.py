from abc import ABC, abstractmethod
from typing import List, Optional

from models.user import User
from sqlalchemy import select
from sqlalchemy.exc import NoResultFound
from sqlalchemy.orm import Session


class IUserRepository(ABC):
    @abstractmethod
    def add(self, user: User) -> User: ...
    @abstractmethod
    def get_by_id(self, user_id: int) -> Optional[User]: ...
    @abstractmethod
    def get_by_email(self, email: str) -> Optional[User]: ...
    @abstractmethod
    def get_all(self, offset: int = 0, limit: int = 100) -> List[User]: ...
    @abstractmethod
    def update(self, user_id: int, fields: dict) -> User: ...
    @abstractmethod
    def delete(self, user_id: int) -> None: ...


class UserRepository(IUserRepository):
    def __init__(self, db: Session):
        self.db = db

    def add(self, user: User) -> User:
        self.db.add(user)
        self.db.commit()
        self.db.refresh(user)
        return user

    def get_by_id(self, user_id: int) -> Optional[User]:
        stmt = select(User).where(User.id == user_id)
        return self.db.scalars(stmt).first()

    def get_by_email(self, email: str) -> Optional[User]:
        stmt = select(User).where(User.email == email)
        return self.db.scalars(stmt).first()

    def get_all(self, offset: int = 0, limit: int = 100) -> List[User]:
        stmt = select(User).order_by(User.id).offset(offset).limit(limit)
        return list(self.db.scalars(stmt))

    def update(self, user_id: int, fields: dict) -> User:
        user = self.get_by_id(user_id)
        if not user:
            raise NoResultFound(f"User with id {user_id} not found.")

        for key, value in fields.items():
            if hasattr(user, key):
                setattr(user, key, value)

        self.db.commit()
        self.db.refresh(user)
        return user

    def delete(self, user_id: int) -> None:
        user = self.get_by_id(user_id)
        if not user:
            raise NoResultFound(f"User with id {user_id} not found.")

        self.db.delete(user)
        self.db.commit()
