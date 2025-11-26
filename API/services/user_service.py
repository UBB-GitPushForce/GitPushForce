from abc import ABC, abstractmethod
from dependencies import di
from fastapi import HTTPException
from models.user import User
from schemas.user import UserCreate, UserLogin, UserUpdate, UserResponse
from schemas.api_response import APIResponse
from utils.helpers.logger import Logger
from utils.helpers.jwt_utils import JwtUtils
from utils.helpers.constants import ACCESS_TOKEN_FIELD, USER_FIELD, HASHED_PASSWORD_FIELD, PASSWORD_FIELD, EMAIL_FIELD, STATUS_BAD_REQUEST
from utils.helpers.password_util import PasswordUtil

class IUserService(ABC):
    """
    Defines the interface for user authentication and account actions.
    """
    @abstractmethod
    def register_user(self, user_in: UserCreate) -> APIResponse: ...
    @abstractmethod
    def get_all_users(self) -> APIResponse: ...
    @abstractmethod
    def login_user(self, user_in: UserLogin) -> APIResponse: ...
    @abstractmethod
    def request_password_reset(self, email: str) -> APIResponse: ...
    @abstractmethod
    def reset_password(self, token: str, new_password: str) -> APIResponse: ...
    @abstractmethod
    def change_password(self, user_id: int, old_password: str, new_password: str) -> APIResponse: ...


class UserService:
    """
    Manages user authentication and CRUDs.
    """
    
    def __init__(self):
        """
        Constructor method.
        """
        self.repository = di.get_user_repository()
        self.logger = Logger()

    def get_all_users(self) -> APIResponse:
        """
        Method for retrieving all users.
        """
        self.logger.info("Fetching all users")
        
        users = self.repository.get_all()
        users_response = [UserResponse.model_validate(user) for user in users]
        
        return APIResponse(
            success=True,
            data=users_response
        )

    def register_user(self, user_in: UserCreate) -> APIResponse:
        """
        Method for registering a new user. Also returns a JWT token.
        """
        self.logger.info(f"Registering user with email={user_in.email}")

        if self.repository.get_by_email(user_in.email):
            self.logger.info(f"Could not register user with email={user_in.email}, reason: email already exists")
            raise HTTPException(status_code=STATUS_BAD_REQUEST, detail="A user with this email already exists.")

        hashed_password = PasswordUtil.hash_password(user_in.password)
        user = User(
            email=user_in.email,
            first_name=user_in.first_name,
            last_name=user_in.last_name,
            phone_number=user_in.phone_number,
            hashed_password=hashed_password,
            budget=user_in.budget
        )

        user_id = self.repository.add(user)
        created_user = self.repository.get_by_id(user_id)
        user_response = UserResponse.model_validate(created_user)
        
        access_token = JwtUtils.encode_token(user_id)

        return APIResponse(
            success=True,
            data={
                ACCESS_TOKEN_FIELD: access_token,
                USER_FIELD: user_response,
            }
        )

    def login_user(self, user_in: UserLogin) -> APIResponse:
        """
        Method for logging a user in. Also returns a JWT token.
        """
        self.logger.info(f"User with email {user_in.email} is attempting to log in.")
        
        user = self.repository.get_by_email(user_in.email)
        if not user or not PasswordUtil.verify_password(user_in.password, user.hashed_password):
            self.logger.info(f"User with email {user_in.email} could not log in because he provided an invalid email or password.")
            raise HTTPException(status_code=401, detail="Invalid email or password.")

        access_token = JwtUtils.encode_token(user.id)
        user_response = UserResponse.model_validate(user)
        
        return APIResponse(
            success=True,
            data={
                ACCESS_TOKEN_FIELD: access_token,
                USER_FIELD: user_response,
            }
        )

    def get_user_by_id(self, user_id: int) -> APIResponse:
        """
        Method for retrieving an user by id.
        """
        self.logger.info(f"Retrieving the user with id {user_id}")
        
        user = self.repository.get_by_id(user_id)
        if not user:
            self.logger.info(f"User with id {user_id} was not found.")
            raise HTTPException(status_code=404, detail="User not found.")
        
        user_response = UserResponse.model_validate(user)
        
        return APIResponse(
            success=True,
            data={
                USER_FIELD: user_response,
            }
        )

    def update_user(self, user_id: int, user_in: UserUpdate) -> APIResponse:
        """
        Method for updating a user's information.
        """
        self.logger.info(f"Updating user with id {user_id}, with values from {user_in}")
        
        fields = user_in.model_dump(exclude_unset=True)
        if PASSWORD_FIELD in fields:
            fields[HASHED_PASSWORD_FIELD] = PasswordUtil.hash_password(fields.pop(PASSWORD_FIELD))
        if EMAIL_FIELD in fields:
            existing_user = self.repository.get_by_email(fields[EMAIL_FIELD])
            if existing_user and existing_user.id != user_id:
                self.logger.info(f"User with id {user_id} tried to change his email to {fields[EMAIL_FIELD]} but it is already used.")
                raise HTTPException(status_code=STATUS_BAD_REQUEST, detail="A user with this email already exists.")

        user = self.repository.update(user_id, fields)
        
        user_response = UserResponse.model_validate(user)
        
        return APIResponse(
            success=True,
            data={
                USER_FIELD: user_response
            }
        )

    def delete_user(self, user_id: int) -> APIResponse:
        """
        Method for deleting a user by id.
        """
        self.logger.info(f"Deleting user with id {user_id}")
        
        self.repository.delete(user_id)
        
        return APIResponse(
            success=True
        )

    def change_password(self, user_id: int, old_password: str, new_password: str) -> APIResponse:
        """
        Verifies the old password and updates to the new password.
        """
        self.logger.info(f"Changing password for user_id={user_id}")
        
        user = self.repository.get_by_id(user_id)
        if not user:
            self.logger.warning(f"Password change attempted for inexistent user with id {user_id}")
            raise HTTPException(status_code=STATUS_BAD_REQUEST, detail="Invalid user.")
        if not PasswordUtil.verify_password(old_password, user.hashed_password):
            self.logger.warning(f"Password change failed for user_id={user_id}: Invalid old password")
            raise HTTPException(status_code=STATUS_BAD_REQUEST, detail="Invalid old password.")

        new_hashed_password = PasswordUtil.hash_password(new_password)

        self.repository.update(user_id, {HASHED_PASSWORD_FIELD: new_hashed_password})
        
        return APIResponse(
            success=True,
        )

    def request_password_reset(self, email: str) -> APIResponse:
        """
        Method for requesting a password reset.
        """
        self.logger.info(f"Password reset request initiated for user with email {email}")
        
        user = self.repository.get_by_email(email)
        if user:
            reset_token = JwtUtils.create_reset_token(user.id)
            # TODO: Send token via email

        return APIResponse(
            success=True,
            message="If the user exists, a password reset token has been sent to their email."
        )

    def reset_password(self, token: str, new_password: str) -> APIResponse:
        """
        Method for resetting password.
        """
        try:
            user_id = JwtUtils.decode_reset_token(token)
        except Exception:
            self.logger.warning(f"Invalid or expired reset token used")
            raise HTTPException(status_code=STATUS_BAD_REQUEST, detail="Invalid or expired token")
        
        user = self.repository.get_by_id(user_id)
        if not user:
            self.logger.warning(f"Attempted password reset for user {user_id}, but it doesn't exist")
            raise HTTPException(status_code=404, detail="User not found.")
        
        self.logger.info(f"Resetting password for user {user.email}")

        self.repository.update(user_id, {
            HASHED_PASSWORD_FIELD: PasswordUtil.hash_password(new_password)
        })
        
        return APIResponse(
            success=True
        )