import pytest
from models.group import Group
from models.user import User
from services.users_groups_service import UsersGroupsService


class MockUsersGroupsRepository:
    """
    Provides an in-memory mock repository for managing user group relations.

    Args:
        None

    Returns:
        MockUsersGroupsRepository instance

    Exceptions:
        None
    """

    def __init__(self):
        """
        Initializes the storage for users, groups, and user-group links.

        Args:
            None

        Returns:
            None

        Exceptions:
            None
        """
        self.users = {}          # user_id -> User
        self.groups = {}         # group_id -> Group
        self.links = []          # list of (user_id, group_id)

    def add_user(self, user: User):
        """
        Adds a user to the internal storage.

        Args:
            user (User) user object to store

        Returns:
            None

        Exceptions:
            None
        """
        self.users[user.id] = user

    def add_group(self, group: Group):
        """
        Adds a group to the internal storage.

        Args:
            group (Group) group object to store

        Returns:
            None

        Exceptions:
            None
        """
        self.groups[group.id] = group

    def add_user_to_group(self, user_id: int, group_id: int):
        """
        Creates a user-to-group relation.

        Args:
            user_id (int) id of the user
            group_id (int) id of the group

        Returns:
            None

        Exceptions:
            None
        """
        self.links.append((user_id, group_id))

    def remove_user_from_group(self, user_id: int, group_id: int):
        """
        Removes a user-to-group relation.

        Args:
            user_id (int) id of the user
            group_id (int) id of the group

        Returns:
            None

        Exceptions:
            None
        """
        self.links = [
            (u, g) for (u, g) in self.links
            if not (u == user_id and g == group_id)
        ]

    def get_groups_by_user(self, user_id: int):
        """
        Retrieves all groups linked to a user.

        Args:
            user_id (int) id of the user

        Returns:
            list list of Group objects

        Exceptions:
            None
        """
        group_ids = [g for (u, g) in self.links if u == user_id]
        return [self.groups[g] for g in group_ids]

    def get_users_by_group(self, group_id: int):
        """
        Retrieves all users linked to a group.

        Args:
            group_id (int) id of the group

        Returns:
            list list of User objects

        Exceptions:
            None
        """
        user_ids = [u for (u, g) in self.links if g == group_id]
        return [self.users[u] for u in user_ids]


@pytest.fixture
def mock_repo():
    """
    Creates a mock repository preloaded with sample users and groups.

    Args:
        None

    Returns:
        MockUsersGroupsRepository repository instance

    Exceptions:
        None
    """
    repo = MockUsersGroupsRepository()
    repo.add_user(User(id=1, first_name="Alice", last_name="Smith", hashed_password="alicepassword", email="a@test.com"))
    repo.add_user(User(id=2, first_name="Bob", last_name="Pop", hashed_password="bobpassword", email="b@test.com"))
    repo.add_group(Group(id=10, name="Friends", description="test"))
    repo.add_group(Group(id=20, name="Work", description="test2"))
    return repo


@pytest.fixture
def service(mock_repo):
    """
    Creates a UsersGroupsService instance using the mock repository.

    Args:
        mock_repo (MockUsersGroupsRepository) preloaded repository

    Returns:
        UsersGroupsService service instance

    Exceptions:
        None
    """
    return UsersGroupsService(mock_repo)


def test_add_user_to_group(service, mock_repo):
    """
    Ensures a user-to-group relation is added successfully.

    Args:
        service (UsersGroupsService) service under test
        mock_repo (MockUsersGroupsRepository) repo storing links

    Returns:
        None

    Exceptions:
        AssertionError on incorrect link creation
    """
    service.add_user_to_group(1, 10)
    assert (1, 10) in mock_repo.links
    assert len(mock_repo.links) == 1


def test_remove_user_from_group(service, mock_repo):
    """
    Verifies removing a user from a group works correctly.

    Args:
        service (UsersGroupsService) service under test
        mock_repo (MockUsersGroupsRepository) repository

    Returns:
        None

    Exceptions:
        AssertionError on unexpected links
    """
    mock_repo.links = [(1, 10), (2, 10)]
    service.delete_user_from_group(1, 10)
    assert (1, 10) not in mock_repo.links
    assert (2, 10) in mock_repo.links
    assert len(mock_repo.links) == 1


def test_get_user_groups(service, mock_repo):
    """
    Ensures retrieving all groups of a user returns correct results.

    Args:
        service (UsersGroupsService) service instance
        mock_repo (MockUsersGroupsRepository) repository

    Returns:
        None

    Exceptions:
        AssertionError on wrong returned group list
    """
    mock_repo.links = [(1, 10), (1, 20), (2, 20)]
    groups = service.get_user_groups(1)
    assert len(groups) == 2
    assert {g.id for g in groups} == {10, 20}

def test_get_users_from_group(service, mock_repo):
    """
    Ensures retrieving all users of a group works correctly.

    Args:
        service (UsersGroupsService) service instance
        mock_repo (MockUsersGroupsRepository) repository

    Returns:
        None

    Exceptions:
        AssertionError on incorrect user list
    """
    mock_repo.links = [(1, 10), (2, 10), (1, 20)]
    users = service.get_users_from_group(10)
    assert len(users) == 2
    assert {u.id for u in users} == {1, 2}
