from pydantic import BaseModel, Field, EmailStr
from typing import Optional
from datetime import datetime

# --- User Schemas ---

class UserBase(BaseModel):
    first_name: str = Field(..., min_length=1, max_length=255)
    last_name: str = Field(..., min_length=1, max_length=255)
    email: EmailStr = Field(..., description="User's unique email address.")
    phone_number: str = Field(..., min_length=1, max_length=50)

class UserCreate(UserBase):
    password: str = Field(..., min_length=8, description="User's password.")

class UserLogin(BaseModel):
    email: EmailStr
    password: str