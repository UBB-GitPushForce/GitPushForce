from abc import ABC, abstractmethod
from typing import Optional

from sqlalchemy.orm import Session

from models.user import User
from schemas.user import UserCreate, UserLogin
from repositories.user_repository import UserRepository

## TODO: Replace with a proper password hashing utility
class PasswordUtil:
    @staticmethod
    def hash_password(password: str) -> str:
        return password
    
    @staticmethod
    def verify_password(plain_password: str, hashed_password: str) -> bool:
        return plain_password == hashed_password


class IAuthService(ABC):
    @abstractmethod
    def register_user(self, user_in: UserCreate) -> User: ...
    @abstractmethod
    def login_user(self, user_in: UserLogin) -> User: ...


class AuthService:
    def __init__(self, db: Session):
        self.repository = UserRepository(db)

    def register_user(self, user_in: UserCreate) -> User:
        """
        Registers a new user: checks for existing email, hashes password, and saves to DB.
        """
        # Check for existing user
        if self.repository.get_by_email(user_in.email):
            raise ValueError("A user with this email already exists.")

        # Hash the password
        hashed_password = PasswordUtil.hash_password(user_in.password)

        # Create the User ORM object
        user = User(
            email=user_in.email,
            first_name=user_in.first_name,
            last_name=user_in.last_name,
            phone_number=user_in.phone_number,
            hashed_password=hashed_password,
        )

        # Save to DB
        return self.repository.add(user)

    def login_user(self, user_in: UserLogin) -> User:
        """
        Authenticates a user: fetches user by email and verifies the password.
        """
        # Get user by email
        user = self.repository.get_by_email(user_in.email)

        # Check if user exists and password is correct
        if not user or not PasswordUtil.verify_password(user_in.password, user.hashed_password):
            raise ValueError("Invalid email or password.")
        
        return user
