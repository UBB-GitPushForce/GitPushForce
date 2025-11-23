from typing import Optional

from pydantic import BaseModel, EmailStr, Field

# --- User Schemas ---


class UserBase(BaseModel):
    """
    Defines the base fields shared by all user schemas.

    Args:
        first_name (str) user first name
        last_name (str) user last name
        email (EmailStr) unique user email
        phone_number (str) user phone number

    Returns:
        UserBase base model for shared fields

    Exceptions:
        None
    """
    first_name: str = Field(..., min_length=1, max_length=255)
    last_name: str = Field(..., min_length=1, max_length=255)
    email: EmailStr = Field(..., description="User's unique email address.")
    phone_number: str = Field(..., min_length=1, max_length=50)

    class Config:
        from_attributes = True


class UserCreate(UserBase):
    """
    Schema for creating a new user.

    Args:
        first_name (str) user first name
        last_name (str) user last name
        email (EmailStr) unique user email
        phone_number (str) user phone number
        password (str) user password

    Returns:
        UserCreate validated user creation data

    Exceptions:
        None
    """
    password: str = Field(..., min_length=8, description="User's password.")
    # budget is NOT included here — DB default is used


class UserLogin(BaseModel):
    """
    Schema for logging in a user.

    Args:
        email (EmailStr) login email
        password (str) login password

    Returns:
        UserLogin login credentials

    Exceptions:
        None
    """
    email: EmailStr
    password: str


class UserUpdate(BaseModel):
    """
    Schema for updating an existing user.

    Args:
        first_name (str) updated first name
        last_name (str) updated last name
        email (EmailStr) updated email
        phone_number (str) updated phone number
        password (str) updated password
        budget (int) updated user budget

    Returns:
        UserUpdate validated update data

    Exceptions:
        None
    """
    first_name: Optional[str] = Field(None, min_length=1, max_length=255)
    last_name: Optional[str] = Field(None, min_length=1, max_length=255)
    email: Optional[EmailStr] = Field(None, description="User's unique email address.")
    phone_number: Optional[str] = Field(None, min_length=1, max_length=50)
    password: Optional[str] = Field(None, min_length=8, description="User's password.")
    budget: Optional[int] = Field(None, ge=0, description="User's budget.")


class UserChangePassword(BaseModel):
    """
    Schema for changing a user's password.

    Args:
        old_password (str) current password for verification
        new_password (str) new password to set

    Returns:
        UserChangePassword validated password change data

    Exceptions:
        None
    """
    old_password: str = Field(..., description="Current password for verification.")
    new_password: str = Field(..., min_length=8, description="New password.")


class UserPasswordReset(BaseModel):
    """
    Schema for resetting a user's password.

    Args:
        token (str) token sent by email
        new_password (str) new password to set

    Returns:
        UserPasswordReset validated reset data

    Exceptions:
        None
    """
    token: str = Field(..., description="Password reset token from email.")
    new_password: str = Field(..., min_length=8, description="User's new password.")


class UserResponse(UserBase):
    """
    Schema returned to clients representing a complete user record.

    Args:
        id (int) unique identifier of the user
        first_name (str) user first name
        last_name (str) user last name
        email (EmailStr) unique user email
        phone_number (str) phone number
        budget (int) user budget

    Returns:
        UserResponse API response model

    Exceptions:
        None
    """
    id: int
    budget: int