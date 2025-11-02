import pytest
from services.user_service import UserService, PasswordUtil
from schemas.user import UserCreate, UserLogin
from fastapi import HTTPException

# Mock UserRepository
class MockUserRepository:
    def __init__(self):
        self.users = {}
        self.next_id = 1

    def get_by_email(self, email):
        return self.users.get(email)

    def add(self, user):
        user.id = self.next_id
        self.next_id += 1
        self.users[user.email] = user
        return user

    def get_by_id(self, user_id):
        for user in self.users.values():
            if user.id == user_id:
                return user
        return None

    def update(self, user_id, fields):
        user = self.get_by_id(user_id)
        if not user:
            return None
        for k, v in fields.items():
            setattr(user, k, v)
        return user

    def delete(self, user_id):
        user = self.get_by_id(user_id)
        if user:
            del self.users[user.email]

@pytest.fixture
def user_service():
    mock_repo = MockUserRepository()
    service = UserService.__new__(UserService)  # bypass __init__
    service.repository = mock_repo
    return service

def test_register_user_success(user_service):
    user_in = UserCreate(
        email="test@example.com",
        password="Password123",
        first_name="Test",
        last_name="User",
        phone_number="123456"
    )
    result = user_service.register_user(user_in)
    assert "access_token" in result
    assert result["user"].email == "test@example.com"

def test_register_duplicate_email(user_service):
    user_in = UserCreate(
        email="dup@example.com",
        password="Password123",
        first_name="Test",
        last_name="User",
        phone_number="123456"
    )
    user_service.register_user(user_in)

    with pytest.raises(HTTPException) as exc_info:
        user_service.register_user(user_in)
    assert exc_info.value.status_code == 400
    assert "already exists" in exc_info.value.detail

def test_login_success(user_service):
    user_in = UserCreate(
        email="login@example.com",
        password="Password123",
        first_name="Test",
        last_name="User",
        phone_number="123456"
    )
    user_service.register_user(user_in)

    login_in = UserLogin(email="login@example.com", password="Password123")
    result = user_service.login_user(login_in)
    assert "access_token" in result
    assert result["user"].email == "login@example.com"

def test_login_wrong_password(user_service):
    user_in = UserCreate(
        email="fail@example.com",
        password="Password123",
        first_name="Test",
        last_name="User",
        phone_number="123456"
    )
    user_service.register_user(user_in)

    login_in = UserLogin(email="fail@example.com", password="WrongPassword")
    with pytest.raises(HTTPException) as exc_info:
        user_service.login_user(login_in)
    assert exc_info.value.status_code == 401

def test_get_user_by_id_success(user_service):
    # Register a user first
    user_in = UserCreate(
        email="getuser@example.com",
        password="Password123",
        first_name="Get",
        last_name="User",
        phone_number="123456"
    )
    result = user_service.register_user(user_in)

    user_id = result["user"].id
    user = user_service.get_user_by_id(user_id)
    assert user.id == user_id
    assert user.email == "getuser@example.com"

def test_get_user_by_id_not_found(user_service):
    # Try fetching a user ID that doesn’t exist
    non_existent_id = 999
    with pytest.raises(HTTPException) as exc_info:
        user_service.get_user_by_id(non_existent_id)
    assert exc_info.value.status_code == 404
    assert "not found" in exc_info.value.detail

