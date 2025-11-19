import os
from abc import ABC, abstractmethod
from datetime import datetime, timedelta

import bcrypt
import jwt
from dotenv import load_dotenv
from fastapi import HTTPException, Request
from fastapi.security import HTTPBearer
from models.user import User
from repositories.user_repository import UserRepository
from schemas.user import UserCreate, UserLogin, UserUpdate
from sqlalchemy.orm import Session
from utils.helpers.logger import Logger

# Load environment variables
load_dotenv()

# JWT config
SECRET_KEY = os.getenv("JWT_SECRET", "supersecretkey")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 72  # 3 days


class PasswordUtil:
    """
    Provides password hashing and verification utilities.
    """

    @staticmethod
    def hash_password(password: str) -> str:
        Logger().debug("Hashing password")
        salt = bcrypt.gensalt()
        hashed = bcrypt.hashpw(password.encode("utf-8"), salt)
        return hashed.decode("utf-8")

    @staticmethod
    def verify_password(plain_password: str, hashed_password: str) -> bool:
        Logger().debug("Verifying password")
        return bcrypt.checkpw(plain_password.encode("utf-8"), hashed_password.encode("utf-8"))


class IUserService(ABC):
    """
    Defines the interface for user authentication and account actions.
    """

    @abstractmethod
    def register_user(self, user_in: UserCreate) -> dict: ...
    @abstractmethod
    def login_user(self, user_in: UserLogin) -> dict: ...
    @abstractmethod
    def request_password_reset(self, email: str) -> dict: ...
    @abstractmethod
    def reset_password(self, token: str, new_password: str) -> dict: ...


class UserService:
    """
    Manages user authentication, JWT handling, password resets, and user updates.
    """

    security = HTTPBearer()
    logger = Logger()

    def __init__(self, db: Session):
        self.repository = UserRepository(db)
        self.logger.debug("UserService initialized with DB session")

    # -------------------------- JWT Methods --------------------------

    def _encode_token(self, user_id: int) -> str:
        self.logger.debug(f"Encoding JWT for user_id={user_id}")
        payload = {
            "sub": str(user_id),
            "exp": datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES),
            "iat": datetime.utcnow()
        }
        return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)

    def _decode_token(self, token: str) -> int:
        self.logger.debug("Decoding JWT token")
        try:
            payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
            return int(payload["sub"])
        except jwt.ExpiredSignatureError:
            raise HTTPException(status_code=401, detail="Token expired.")
        except jwt.InvalidTokenError:
            raise HTTPException(status_code=401, detail="Invalid token.")

    def auth_wrapper(self, request: Request):
        auth_header = request.headers.get("Authorization")
        token = None
        if auth_header and auth_header.startswith("Bearer "):
            token = auth_header.split(" ")[1]
        elif "access_token" in request.cookies:
            token = request.cookies["access_token"]

        if not token:
            raise HTTPException(status_code=401, detail="Missing authentication token.")
        return self._decode_token(token)

    # -------------------------- User Methods --------------------------

    def get_all_users(self):
        self.logger.info("Fetching all users")
        return self.repository.get_all()

    def register_user(self, user_in: UserCreate) -> dict:
        """
        Registers a new user and issues a JWT token.
        """
        self.logger.info(f"Registering user with email={user_in.email}")

        if self.repository.get_by_email(user_in.email):
            raise HTTPException(status_code=400, detail="A user with this email already exists.")

        hashed_password = PasswordUtil.hash_password(user_in.password)
        user = User(
            email=user_in.email,
            first_name=user_in.first_name,
            last_name=user_in.last_name,
            phone_number=user_in.phone_number,
            hashed_password=hashed_password,
            # budget defaults to DB default (100)
        )

        user_id = self.repository.add(user)
        token = self._encode_token(user_id)

        # return full user object
        created_user = self.repository.get_by_id(user_id)

        return {
            "access_token": token,
            "user": created_user
        }

    def login_user(self, user_in: UserLogin) -> dict:
        """
        Authenticates a user and returns a JWT token.
        """
        user = self.repository.get_by_email(user_in.email)
        if not user or not PasswordUtil.verify_password(user_in.password, user.hashed_password):
            raise HTTPException(status_code=401, detail="Invalid email or password.")

        token = self._encode_token(user.id)
        return {"access_token": token, "user": user}

    def get_user_by_id(self, user_id: int) -> User:
        user = self.repository.get_by_id(user_id)
        if not user:
            raise HTTPException(status_code=404, detail="User not found.")
        return user

    def update_user(self, user_id: int, user_in: UserUpdate) -> User:
        """
        Updates a user's information.
        """
        fields = user_in.model_dump(exclude_unset=True)

        # Handle password update
        if "password" in fields:
            fields["hashed_password"] = PasswordUtil.hash_password(fields.pop("password"))

        # Handle email uniqueness
        if "email" in fields:
            existing_user = self.repository.get_by_email(fields["email"])
            if existing_user and existing_user.id != user_id:
                raise HTTPException(status_code=400, detail="A user with this email already exists.")

        # Budget handled naturally (if present in fields)
        user = self.repository.update(user_id, fields)
        return user

    def delete_user(self, user_id: int) -> None:
        self.repository.delete(user_id)

    # -------------------------- Password Reset --------------------------

    def request_password_reset(self, email: str) -> dict:
        user = self.repository.get_by_email(email)
        if not user:
            return {"message": "Check the API console for the token."}

        reset_token = self._create_reset_token(user.id)
        print(f"The reset token = {reset_token}")
        return {"message": "Check the API console for the token."}

    def _create_reset_token(self, user_id: int) -> str:
        payload = {
            "sub": str(user_id),
            "exp": datetime.utcnow() + timedelta(minutes=60),
            "iat": datetime.utcnow(),
            "type": "password_reset"
        }
        return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)

    def _decode_reset_token(self, token: str) -> int:
        try:
            payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
            if payload.get("type") != "password_reset":
                raise HTTPException(status_code=400, detail="Invalid reset token.")
            return int(payload["sub"])
        except jwt.ExpiredSignatureError:
            raise HTTPException(status_code=400, detail="Reset token has expired.")
        except jwt.InvalidTokenError:
            raise HTTPException(status_code=400, detail="Invalid reset token.")

    def reset_password(self, token: str, new_password: str) -> dict:
        user_id = self._decode_reset_token(token)
        user = self.repository.get_by_id(user_id)
        if not user:
            raise HTTPException(status_code=404, detail="User not found.")

        self.repository.update(user_id, {
            "hashed_password": PasswordUtil.hash_password(new_password)
        })

        return {"message": "Password has been reset successfully."}
