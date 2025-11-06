from pydantic import BaseModel, Field, EmailStr
from typing import Optional

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

class UserUpdate(BaseModel):
    first_name: Optional[str] = Field(None, min_length=1, max_length=255)
    last_name: Optional[str] = Field(None, min_length=1, max_length=255)
    email: Optional[EmailStr] = Field(None, description="User's unique email address.")
    phone_number: Optional[str] = Field(None, min_length=1, max_length=50)
    password: Optional[str] = Field(None, min_length=8, description="User's password.")

class UserPasswordReset(BaseModel):
    token: str = Field(..., description="Password reset token from email.")
    new_password: str = Field(..., min_length=8, description="User's new password.")