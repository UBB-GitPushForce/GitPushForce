import os
import jwt
import bcrypt
from abc import ABC, abstractmethod
from datetime import datetime, timedelta
from fastapi import HTTPException, Depends, Request
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session
from dotenv import load_dotenv

from models.user import User
from schemas.user import UserCreate, UserLogin
from repositories.user_repository import UserRepository

# Load environment variables
load_dotenv()

# JWT config
SECRET_KEY = os.getenv("JWT_SECRET", "supersecretkey")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 72  # 3 days

class PasswordUtil:
    @staticmethod
    def hash_password(password: str) -> str:
        # Generate a salt and hash the password
        salt = bcrypt.gensalt()
        hashed = bcrypt.hashpw(password.encode("utf-8"), salt)
        return hashed.decode("utf-8")

    @staticmethod
    def verify_password(plain_password: str, hashed_password: str) -> bool:
        return bcrypt.checkpw(plain_password.encode("utf-8"), hashed_password.encode("utf-8"))


class IUserService(ABC):
    @abstractmethod
    def register_user(self, user_in: UserCreate) -> dict: ...
    @abstractmethod
    def login_user(self, user_in: UserLogin) -> dict: ...


class UserService:
    security = HTTPBearer()  # for FastAPI dependency

    def __init__(self, db: Session):
        self.repository = UserRepository(db)

    def _encode_token(self, user_id: int) -> str:
        payload = {
            "sub": str(user_id),
            "exp": datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES),
            "iat": datetime.utcnow()
        }
        return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)

    def _decode_token(self, token: str) -> int:
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


    def register_user(self, user_in: UserCreate) -> dict:
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
        user = self.repository.add(user)
        token = self._encode_token(user.id)
        return {"access_token": token, "user": user}

    def login_user(self, user_in: UserLogin) -> dict:
        user = self.repository.get_by_email(user_in.email)
        if not user or not PasswordUtil.verify_password(user_in.password, user.hashed_password):
            raise HTTPException(status_code=401, detail="Invalid email or password.")
        token = self._encode_token(user.id)
        return {"access_token": token, "user": user}
