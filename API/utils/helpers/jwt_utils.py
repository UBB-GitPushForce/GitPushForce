import os
import jwt

from fastapi import HTTPException, Request
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo

ROMANIA_TZ = ZoneInfo("Europe/Bucharest")
SECRET_KEY = os.getenv("JWT_SECRET", "supersecretkey")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 72  

class JwtUtils:
    @staticmethod
    def encode_token(user_id: int) -> str:
        payload = {
            "sub": str(user_id),
            "exp": datetime.now(ROMANIA_TZ) + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES),
            "iat": datetime.now(ROMANIA_TZ)
        }
        return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)

    @staticmethod
    def decode_token(token: str) -> int:
        try:
            payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
            return int(payload["sub"])
        except jwt.ExpiredSignatureError:
            raise HTTPException(status_code=401, detail="Token expired.")
        except jwt.InvalidTokenError:
            raise HTTPException(status_code=401, detail="Invalid token.")

    @staticmethod
    def auth_wrapper(request: Request):
        auth_header = request.headers.get("Authorization")
        token = None
        if auth_header and auth_header.startswith("Bearer "):
            token = auth_header.split(" ")[1]
        elif "access_token" in request.cookies:
            token = request.cookies["access_token"]

        if not token:
            raise HTTPException(status_code=401, detail="Missing authentication token.")
        return JwtUtils.decode_token(token)
    
    @staticmethod
    def create_reset_token(user_id: int) -> str:
        payload = {
            "sub": str(user_id),
            "exp": datetime.now(ROMANIA_TZ) + timedelta(minutes=60),
            "iat": datetime.now(ROMANIA_TZ),
            "type": "password_reset"
        }
        return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)

    @staticmethod
    def decode_reset_token(token: str) -> int:
        try:
            payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
            if payload.get("type") != "password_reset":
                raise HTTPException(status_code=400, detail="Invalid reset token.")
            return int(payload["sub"])
        except jwt.ExpiredSignatureError:
            raise HTTPException(status_code=400, detail="Reset token has expired.")
        except jwt.InvalidTokenError:
            raise HTTPException(status_code=400, detail="Invalid reset token.")