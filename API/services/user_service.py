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

    Args:
        password (str) input password
        plain_password (str) provided password
        hashed_password (str) stored hashed password

    Returns:
        str for hashed password or bool for verification result

    Exceptions:
        None
    """

    @staticmethod
    def hash_password(password: str) -> str:
        """
        Hashes a plaintext password.

        Args:
            password (str) the raw password

        Returns:
            str hashed password string

        Exceptions:
            None
        """
        Logger().debug("Hashing password")
        salt = bcrypt.gensalt()
        hashed = bcrypt.hashpw(password.encode("utf-8"), salt)
        return hashed.decode("utf-8")

    @staticmethod
    def verify_password(plain_password: str, hashed_password: str) -> bool:
        """
        Verifies a password against its hashed version.

        Args:
            plain_password (str) user-provided password
            hashed_password (str) stored hashed password

        Returns:
            bool result of comparison

        Exceptions:
            None
        """
        Logger().debug("Verifying password")
        return bcrypt.checkpw(plain_password.encode("utf-8"), hashed_password.encode("utf-8"))


class IUserService(ABC):
    """
    Defines the interface for user authentication and account actions.

    Args:
        user_in (UserCreate or UserLogin) user-provided data
        token (str) authentication token
        new_password (str) updated password
        email (str) user email

    Returns:
        dict response payloads

    Exceptions:
        HTTPException raised for invalid credentials or conflicting data
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

    Args:
        db (Session) active database session

    Returns:
        UserService instance

    Exceptions:
        HTTPException raised for invalid tokens or failed authentication
    """

    security = HTTPBearer()
    logger = Logger()

    def __init__(self, db: Session):
        """
        Initializes the UserService with a database session.

        Args:
            db (Session) database connection

        Returns:
            None

        Exceptions:
            None
        """
        self.repository = UserRepository(db)
        self.logger.debug("UserService initialized with DB session")

    # -------------------------- JWT Methods --------------------------

    def _encode_token(self, user_id: int) -> str:
        """
        Creates a JWT access token.

        Args:
            user_id (int) id of the user

        Returns:
            str encoded JWT token

        Exceptions:
            None
        """
        self.logger.debug(f"Encoding JWT for user_id={user_id}")
        payload = {
            "sub": str(user_id),
            "exp": datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES),
            "iat": datetime.utcnow()
        }
        token = jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)
        self.logger.debug("JWT successfully encoded")
        return token

    def _decode_token(self, token: str) -> int:
        """
        Validates and decodes a JWT token.

        Args:
            token (str) JWT token string

        Returns:
            int user id extracted from token

        Exceptions:
            HTTPException raised for expired or invalid tokens
        """
        self.logger.debug("Decoding JWT token")
        try:
            payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
            self.logger.debug(f"Token decoded successfully for user_id={payload.get('sub')}")
            return int(payload["sub"])
        except jwt.ExpiredSignatureError:
            self.logger.warning("Token expired")
            raise HTTPException(status_code=401, detail="Token expired.")
        except jwt.InvalidTokenError:
            self.logger.error("Invalid token")
            raise HTTPException(status_code=401, detail="Invalid token.")

    def auth_wrapper(self, request: Request):
        """
        Extracts and validates the authentication token from headers or cookies.

        Args:
            request (Request) incoming HTTP request

        Returns:
            int authenticated user's id

        Exceptions:
            HTTPException raised when token is missing or invalid
        """
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
        """
        Fetches all users.

        Args:
            None

        Returns:
            list all stored user objects

        Exceptions:
            None
        """
        self.logger.info("Fetching all users")
        return self.repository.get_all()

    def register_user(self, user_in: UserCreate) -> dict:
        """
        Registers a new user and issues a JWT token.

        Args:
            user_in (UserCreate) user registration data

        Returns:
            dict containing access token and user id

        Exceptions:
            HTTPException raised if email is already used
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
        )

        id = self.repository.add(user)
        token = self._encode_token(id)

        self.logger.info(f"User registered successfully with id={id}")
        return {"access_token": token, "user": id}

    def login_user(self, user_in: UserLogin) -> dict:
        """
        Authenticates a user and returns a JWT token.

        Args:
            user_in (UserLogin) login credentials

        Returns:
            dict containing token and user data

        Exceptions:
            HTTPException raised for invalid email or password
        """
        self.logger.info(f"User attempting login: {user_in.email}")

        user = self.repository.get_by_email(user_in.email)
        if not user or not PasswordUtil.verify_password(user_in.password, user.hashed_password):
            self.logger.warning(f"Failed login attempt for {user_in.email}")
            raise HTTPException(status_code=401, detail="Invalid email or password.")

        token = self._encode_token(user.id)
        self.logger.info(f"User logged in successfully: id={user.id}")
        return {"access_token": token, "user": user}

    def get_user_by_id(self, user_id: int) -> User:
        """
        Retrieves a user by their id.

        Args:
            user_id (int) id of the user

        Returns:
            User matching user object

        Exceptions:
            HTTPException raised when user does not exist
        """
        self.logger.debug(f"Fetching user by id={user_id}")
        user = self.repository.get_by_id(user_id)
        if not user:
            self.logger.warning(f"User not found: id={user_id}")
            raise HTTPException(status_code=404, detail="User not found.")
        self.logger.debug(f"User fetched: id={user.id}")
        return user

    def update_user(self, user_id: int, user_in: UserUpdate) -> User:
        """
        Updates a user's information.

        Args:
            user_id (int) id of the user
            user_in (UserUpdate) update payload

        Returns:
            User updated user object

        Exceptions:
            HTTPException raised for email conflicts
        """
        self.logger.info(f"Updating user id={user_id}")

        fields = user_in.model_dump(exclude_unset=True)
        if "password" in fields:
            self.logger.debug("Updating password for user")
            fields["hashed_password"] = PasswordUtil.hash_password(fields.pop("password"))
        if "email" in fields:
            existing_user = self.repository.get_by_email(fields["email"])
            if existing_user and existing_user.id != user_id:
                self.logger.warning(f"Email conflict for update: {fields['email']}")
                raise HTTPException(status_code=400, detail="A user with this email already exists.")

        user = self.repository.update(user_id, fields)
        self.logger.info(f"User updated successfully id={user_id}")
        return user

    def delete_user(self, user_id: int) -> None:
        """
        Deletes a user account.

        Args:
            user_id (int) id of the user

        Returns:
            None

        Exceptions:
            None
        """
        self.logger.info(f"Deleting user id={user_id}")
        self.repository.delete(user_id)
        self.logger.info(f"User deleted id={user_id}")

    # -------------------------- Password Reset --------------------------

    def request_password_reset(self, email: str) -> dict:
        """
        Generates a password reset token for a user.

        Args:
            email (str) user's email

        Returns:
            dict message confirming reset process

        Exceptions:
            None (even if user does not exist)
        """
        self.logger.info(f"Password reset requested for email={email}")
        user = self.repository.get_by_email(email)
        if not user:
            self.logger.warning(f"Password reset request for non-existing email={email}")
            return {"message": "Check the API console for the token."}

        reset_token = self._create_reset_token(user.id)
        self.logger.info(f"Password reset token created for user_id={user.id}")
        print(f"The reset token = {reset_token}")
        return {"message": "Check the API console for the token."}

    def _create_reset_token(self, user_id: int) -> str:
        """
        Creates a short-lived token for password resets.

        Args:
            user_id (int) id of the user

        Returns:
            str encoded reset token

        Exceptions:
            None
        """
        self.logger.debug(f"Creating reset token for user_id={user_id}")
        payload = {
            "sub": str(user_id),
            "exp": datetime.utcnow() + timedelta(minutes=60),
            "iat": datetime.utcnow(),
            "type": "password_reset"
        }
        return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)

    def _decode_reset_token(self, token: str) -> int:
        """
        Validates and decodes a password reset token.

        Args:
            token (str) reset token

        Returns:
            int user id extracted from token

        Exceptions:
            HTTPException raised for expired or invalid tokens
        """
        self.logger.debug("Decoding password reset token")
        try:
            payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
            if payload.get("type") != "password_reset":
                self.logger.error("Invalid reset token type")
                raise HTTPException(status_code=400, detail="Invalid reset token.")
            return int(payload["sub"])
        except jwt.ExpiredSignatureError:
            self.logger.warning("Password reset token expired")
            raise HTTPException(status_code=400, detail="Reset token has expired.")
        except jwt.InvalidTokenError:
            self.logger.error("Invalid reset token")
            raise HTTPException(status_code=400, detail="Invalid reset token.")

    def reset_password(self, token: str, new_password: str) -> dict:
        """
        Resets a user's password using a valid reset token.

        Args:
            token (str) password reset token
            new_password (str) new confidential password

        Returns:
            dict confirmation message

        Exceptions:
            HTTPException raised if token is invalid or user does not exist
        """
        self.logger.info("Processing password reset")
        user_id = self._decode_reset_token(token)
        user = self.repository.get_by_id(user_id)
        if not user:
            self.logger.error(f"Password reset failed, user not found id={user_id}")
            raise HTTPException(status_code=404, detail="User not found.")

        self.repository.update(user_id, {"hashed_password": PasswordUtil.hash_password(new_password)})
        self.logger.info(f"Password reset successful for user id={user_id}")
        return {"message": "Password has been reset successfully."}
