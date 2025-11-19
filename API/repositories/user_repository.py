from abc import ABC, abstractmethod
from typing import List, Optional

from models.user import User
from sqlalchemy import select
from sqlalchemy.orm import Session


class IUserRepository(ABC):
    # CREATE
    @abstractmethod
    def add(self, user: User) -> None: ...

    # READ
    @abstractmethod
    def get_by_id(self, user_id: int) -> Optional[User]: ...
    @abstractmethod
    def get_by_email(self, email: str) -> Optional[User]: ...
    @abstractmethod
    def get_all(self, offset: int = 0, limit: int = 100) -> List[User]: ...

    # UPDATE
    @abstractmethod
    def update(self, user_id: int, fields: dict) -> None: ...

    # DELETE
    @abstractmethod
    def delete(self, user_id: int) -> None: ...


class UserRepository(IUserRepository):
    def __init__(self, db: Session):
        self.db = db

    def add(self, user: User) -> int:
        """
        Creates a new user.

        Args:
            user (User) user to add

        Returns:
            int id of the saved user

        Exceptions:
            None
        """

        self.db.add(user)
        self.db.commit()
        self.db.refresh(user)
        return user.id

    def get_by_id(self, user_id: int) -> Optional[User]:
        """
        Retrieves one user by id.

        Args:
            user_id (int) id of the user

        Returns:
            User or None matching user or no result

        Exceptions:
            None
        """

        stmt = select(User).where(User.id == user_id)
        return self.db.scalars(stmt).first()

    def get_by_email(self, email: str) -> Optional[User]:
        """
        Retrieves one user by email.

        Args:
            email (str) email of the user

        Returns:
            User or None matching user or no result

        Exceptions:
            None
        """

        stmt = select(User).where(User.email == email)
        return self.db.scalars(stmt).first()

    def get_all(self, offset: int = 0, limit: int = 100) -> List[User]:
        """
        Retrieves all users with pagination.

        Args:
            offset (int) items to skip
            limit (int) maximum items to return

        Returns:
            list[User] paginated users

        Exceptions:
            None
        """

        stmt = select(User).order_by(User.id).offset(offset).limit(limit)
        return list(self.db.scalars(stmt))

    def update(self, user_id: int, fields: dict) -> None:
        """
        Updates specific fields of a user.

        Args:
            user_id (int) id of the user
            fields (dict) key value fields to update

        Returns:
            None no return value

        Exceptions:
            KeyError raised when a field does not exist on the model
        """

        user = self.get_by_id(user_id)
        for key, value in fields.items():
            if hasattr(user, key):
                setattr(user, key, value)
        self.db.commit()
        self.db.refresh(user)

    def delete(self, user_id: int) -> None:
        """
        Removes a user by id.

        Args:
            user_id (int) id of the user

        Returns:
            None no return value

        Exceptions:
            None
        """

        user = self.get_by_id(user_id)
        self.db.delete(user)
        self.db.commit()
