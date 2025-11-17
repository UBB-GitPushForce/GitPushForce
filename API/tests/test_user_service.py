import jwt
import pytest
from fastapi import HTTPException
from schemas.user import UserCreate, UserLogin, UserUpdate
from services.user_service import ALGORITHM, SECRET_KEY, UserService


# Mock UserRepository
class MockUserRepository:
    """
    Provides an in-memory mock repository for testing user operations.

    Args:
        None

    Returns:
        MockUserRepository instance

    Exceptions:
        None
    """

    def __init__(self):
        """
        Initializes empty user storage.

        Args:
            None

        Returns:
            None

        Exceptions:
            None
        """
        self.users = {}
        self.next_id = 1

    def get_by_email(self, email):
        """
        Retrieves a stored user by email.

        Args:
            email (str) user email

        Returns:
            User matching the email or None

        Exceptions:
            None
        """
        return self.users.get(email)

    def add(self, user):
        """
        Adds a user to in-memory storage and assigns an ID.

        Args:
            user (User) user object

        Returns:
            User newly stored user

        Exceptions:
            None
        """
        user.id = self.next_id
        self.next_id += 1
        self.users[user.email] = user
        return user

    def get_all(self):
        """
        Returns all stored users.

        Args:
            None

        Returns:
            list list of User objects

        Exceptions:
            None
        """
        return list(self.users.values())

    def get_by_id(self, user_id):
        """
        Retrieves a user by ID.

        Args:
            user_id (int) id of the user

        Returns:
            User or None if not found

        Exceptions:
            None
        """
        for user in self.users.values():
            if user.id == user_id:
                return user
        return None

    def update(self, user_id, fields):
        """
        Updates a user’s fields.

        Args:
            user_id (int) id of the user
            fields (dict) fields to update

        Returns:
            User updated user or None

        Exceptions:
            None
        """
        user = self.get_by_id(user_id)
        if not user:
            return None
        for k, v in fields.items():
            setattr(user, k, v)
        return user

    def delete(self, user_id):
        """
        Deletes a user by ID.

        Args:
            user_id (int) target user id

        Returns:
            None

        Exceptions:
            None
        """
        user = self.get_by_id(user_id)
        if user:
            del self.users[user.email]


class MockRequest:
    """
    Represents a minimal mock Request object.

    Args:
        headers (dict) optional request headers
        cookies (dict) optional request cookies

    Returns:
        MockRequest instance

    Exceptions:
        None
    """
    def __init__(self, headers=None, cookies=None):
        """
        Initializes mock headers and cookies.

        Args:
            headers (dict) request headers
            cookies (dict) request cookies

        Returns:
            None

        Exceptions:
            None
        """
        self.headers = headers or {}
        self.cookies = cookies or {}


@pytest.fixture
def user_service():
    """
    Creates a UserService instance using a mock repository.

    Args:
        None

    Returns:
        UserService service instance

    Exceptions:
        None
    """
    mock_repo = MockUserRepository()
    service = UserService.__new__(UserService)  # bypass __init__
    service.repository = mock_repo
    return service


def test_register_user_success(user_service):
    """
    Ensures a valid user registers successfully.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        AssertionError on failure
    """
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
    """
    Ensures duplicate emails raise an HTTP 400 error.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        HTTPException when email already exists
    """
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
    """
    Ensures a user can log in with correct credentials.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        AssertionError on failure
    """
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
    """
    Ensures login fails when password is incorrect.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        HTTPException on invalid credentials
    """
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
    """
    Ensures user lookup by ID returns correct data.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        AssertionError if lookup fails
    """
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
    """
    Ensures lookup of missing user ID raises HTTP 404.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        HTTPException for missing user
    """
    with pytest.raises(HTTPException) as exc_info:
        user_service.get_user_by_id(999)
    assert exc_info.value.status_code == 404


def test_get_all_users(user_service):
    """
    Ensures fetching all users returns correct count.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        AssertionError on incorrect count
    """
    user_service.register_user(UserCreate(
        email="u1@example.com", password="Password123",
        first_name="A", last_name="A", phone_number="1"))

    user_service.register_user(UserCreate(
        email="u2@example.com", password="Password123",
        first_name="B", last_name="B", phone_number="2"))

    all_users = user_service.get_all_users()
    assert len(all_users) == 2


def test_update_user_success(user_service):
    """
    Ensures updating a user field works properly.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        AssertionError on failure
    """
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
    """
    Ensures updating to an email already in use raises HTTP 400.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        HTTPException for email duplication
    """
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

    with pytest.raises(HTTPException) as exc:
        user_service.update_user(u1, UserUpdate(email="b@example.com"))

    assert exc.value.status_code == 400


def test_delete_user(user_service):
    """
    Ensures user deletion removes the record.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        HTTPException if user still exists
    """
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
    """
    Ensures requesting reset for a nonexistent email does not raise an error.

    Args:
        user_service (UserService) service under test
        capsys (pytest fixture) console capture

    Returns:
        None

    Exceptions:
        None
    """
    result = user_service.request_password_reset("none@example.com")
    assert "Check the API console" in result["message"]


def test_request_password_reset_success(user_service, capsys):
    """
    Ensures password reset request works for existing users.

    Args:
        user_service (UserService) service under test
        capsys (pytest fixture) console capture

    Returns:
        None

    Exceptions:
        None
    """
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
    """
    Ensures a valid reset token updates the password.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        AssertionError on unexpected result
    """
    reg = user_service.register_user(UserCreate(
        email="pr@example.com",
        password="Password123",
        first_name="A",
        last_name="B",
        phone_number="123",
    ))
    user_id = reg["user"].id

    token = user_service._create_reset_token(user_id)

    out = user_service.reset_password(token, "NewPass123")
    assert out["message"] == "Password has been reset successfully."


def test_reset_password_expired_token(user_service):
    """
    Ensures expired reset tokens are rejected.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        HTTPException for expired token
    """
    token = jwt.encode(
        {
            "sub": "1",
            "type": "password_reset",
            "exp": 0
        },
        SECRET_KEY,
        algorithm=ALGORITHM
    )
    with pytest.raises(HTTPException) as exc:
        user_service.reset_password(token, "NewPass123")
    assert exc.value.status_code == 400
    assert "expired" in exc.value.detail.lower()


def test_reset_password_invalid_token(user_service):
    """
    Ensures invalid reset tokens raise an error.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        HTTPException on invalid token
    """
    with pytest.raises(HTTPException):
        user_service.reset_password("invalid", "NewPass123")


def test_reset_password_user_not_found(user_service):
    """
    Ensures reset fails if the user from token does not exist.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        HTTPException for missing user
    """
    token = jwt.encode(
        {"sub": "999", "type": "password_reset"},
        SECRET_KEY,
        algorithm=ALGORITHM
    )

    with pytest.raises(HTTPException) as exc:
        user_service.reset_password(token, "NewPass123")

    assert exc.value.status_code == 404


def test_auth_wrapper_header_token(user_service):
    """
    Ensures authentication works using Authorization header.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        AssertionError on wrong ID
    """
    token = user_service._encode_token(42)
    req = MockRequest(headers={"Authorization": f"Bearer {token}"})
    user_id = user_service.auth_wrapper(req)
    assert user_id == 42


def test_auth_wrapper_cookie_token(user_service):
    """
    Ensures authentication works using cookie tokens.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        AssertionError on mismatch
    """
    token = user_service._encode_token(55)
    req = MockRequest(cookies={"access_token": token})
    user_id = user_service.auth_wrapper(req)
    assert user_id == 55


def test_auth_wrapper_missing_token(user_service):
    """
    Ensures requests without tokens are rejected.

    Args:
        user_service (UserService) service under test

    Returns:
        None

    Exceptions:
        HTTPException when no token is found
    """
    req = MockRequest()
    with pytest.raises(HTTPException):
        user_service.auth_wrapper(req)
