import jwt
import pytest
from fastapi import HTTPException
from schemas.user import UserCreate, UserLogin, UserUpdate
from services.user_service import ALGORITHM, SECRET_KEY, UserService


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

    def get_all(self):
        return list(self.users.values())

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

class MockRequest:
    """A minimal fake request object."""
    def __init__(self, headers=None, cookies=None):
        self.headers = headers or {}
        self.cookies = cookies or {}

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


def test_get_all_users(user_service):
    user_service.register_user(UserCreate(
        email="u1@example.com", password="Password123",
        first_name="A", last_name="A", phone_number="1"))

    user_service.register_user(UserCreate(
        email="u2@example.com", password="Password123",
        first_name="B", last_name="B", phone_number="2"))

    all_users = user_service.get_all_users()
    assert len(all_users) == 2


def test_update_user_success(user_service):
    # Register a user
    user_in = UserCreate(
        email="update@example.com",
        password="Password123",
        first_name="Old",
        last_name="Name",
        phone_number="123456"
    )
    result = user_service.register_user(user_in)
    user_id = result["user"].id

    update_in = UserUpdate(first_name="NewName")

    updated = user_service.update_user(user_id, update_in)
    assert updated.first_name == "NewName"


def test_update_user_email_conflict(user_service):
    # Register 2 users
    u1 = user_service.register_user(UserCreate(
        email="a@example.com",
        password="Password123",
        first_name="A",
        last_name="A",
        phone_number="123",
    ))["user"]

    user_service.register_user(UserCreate(
        email="b@example.com",
        password="Password123",
        first_name="B",
        last_name="B",
        phone_number="123",
    ))

    # Attempt to change user1 email to "b@example.com"
    with pytest.raises(HTTPException) as exc:
        user_service.update_user(u1, UserUpdate(email="b@example.com"))

    assert exc.value.status_code == 400


def test_delete_user(user_service):
    r = user_service.register_user(UserCreate(
        email="delete@example.com",
        password="Password123",
        first_name="Del",
        last_name="User",
        phone_number="000",
    ))
    user_id = r["user"]

    user_service.delete_user(user_id)

    with pytest.raises(HTTPException):
        user_service.get_user_by_id(user_id)


def test_request_password_reset_nonexistent(user_service, capsys):
    # Email does not exist → should NOT throw
    result = user_service.request_password_reset("none@example.com")
    assert "Check the API console" in result["message"]


def test_request_password_reset_success(user_service, capsys):
    user_service.register_user(UserCreate(
        email="reset@example.com",
        password="Password123",
        first_name="A",
        last_name="B",
        phone_number="123",
    ))

    result = user_service.request_password_reset("reset@example.com")
    assert "Check the API console" in result["message"]


def test_reset_password_success(user_service):
    reg = user_service.register_user(UserCreate(
        email="pr@example.com",
        password="Password123",
        first_name="A",
        last_name="B",
        phone_number="123",
    ))
    user_id = reg["user"].id

    # Make real reset token
    token = user_service._create_reset_token(user_id)

    out = user_service.reset_password(token, "NewPass123")
    assert out["message"] == "Password has been reset successfully."


def test_reset_password_expired_token(user_service):
    token = jwt.encode(
        {
            "sub": "1",
            "type": "password_reset",
            "exp": 0  # already expired
        },
        SECRET_KEY,
        algorithm=ALGORITHM
    )
    with pytest.raises(HTTPException) as exc:
        user_service.reset_password(token, "NewPass123")
    assert exc.value.status_code == 400
    assert "expired" in exc.value.detail.lower()


def test_reset_password_invalid_token(user_service):
    with pytest.raises(HTTPException):
        user_service.reset_password("invalid", "NewPass123")


def test_reset_password_user_not_found(user_service):
    # Generate a token for user id=999 even if user doesn't exist
    token = jwt.encode(
        {"sub": "999", "type": "password_reset"},
        SECRET_KEY,
        algorithm=ALGORITHM
    )

    with pytest.raises(HTTPException) as exc:
        user_service.reset_password(token, "NewPass123")

    assert exc.value.status_code == 404


def test_auth_wrapper_header_token(user_service):
    token = user_service._encode_token(42)
    req = MockRequest(headers={"Authorization": f"Bearer {token}"})
    user_id = user_service.auth_wrapper(req)
    assert user_id == 42


def test_auth_wrapper_cookie_token(user_service):
    token = user_service._encode_token(55)
    req = MockRequest(cookies={"access_token": token})
    user_id = user_service.auth_wrapper(req)
    assert user_id == 55


def test_auth_wrapper_missing_token(user_service):
    req = MockRequest()
    with pytest.raises(HTTPException):
        user_service.auth_wrapper(req)

