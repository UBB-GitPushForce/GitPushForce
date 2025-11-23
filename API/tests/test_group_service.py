import pytest
from models.group import Group
from schemas.group import GroupCreate, GroupUpdate
from services.group_service import GroupService
from sqlalchemy.exc import NoResultFound


class MockGroupRepository:
    """
    Provides an in-memory mock implementation of a group repository for testing.

    Args:
        None

    Returns:
        MockGroupRepository instance

    Exceptions:
        None
    """

    def __init__(self):
        """
        Initializes group storage and an id counter.

        Args:
            None

        Returns:
            None

        Exceptions:
            None
        """
        self.groups = {}
        self.counter = 1

    def add(self, group: Group):
        """
        Adds a new group to the repository.

        Args:
            group (Group) group model to persist

        Returns:
            Group stored group with assigned id

        Exceptions:
            None
        """
        group.id = self.counter
        # Ensure invitation_code is present for tests that might mock objects directly
        if not hasattr(group, "invitation_code") or not group.invitation_code:
            group.invitation_code = f"CODE{self.counter}"
            
        self.groups[self.counter] = group
        self.counter += 1
        return group

    def get_by_id(self, group_id: int):
        """
        Retrieves a group by its id.

        Args:
            group_id (int) id of the group

        Returns:
            Group or None if not found

        Exceptions:
            None
        """
        return self.groups.get(group_id)

    def get_by_name(self, name: str):
        """
        Retrieves a group by name.

        Args:
            name (str) group name to search

        Returns:
            Group or None if missing

        Exceptions:
            None
        """
        for g in self.groups.values():
            if g.name == name:
                return g
        return None

    def get_by_invitation_code(self, code: str):
        """
        Retrieves a group by invitation code.

        Args:
            code (str) unique invitation code

        Returns:
            Group or None matching group

        Exceptions:
            None
        """
        for g in self.groups.values():
            if hasattr(g, "invitation_code") and g.invitation_code == code:
                return g
        return None

    def get_all(self, offset: int = 0, limit: int = 100):
        """
        Returns groups with pagination.

        Args:
            offset (int) starting index
            limit (int) maximum number of items

        Returns:
            list group list within range

        Exceptions:
            None
        """
        groups = list(self.groups.values())
        return groups[offset: offset + limit]

    def update(self, group_id: int, fields: dict):
        """
        Updates selected fields of a group.

        Args:
            group_id (int) id of the group
            fields (dict) fields to modify

        Returns:
            Group updated group

        Exceptions:
            None
        """
        group = self.groups.get(group_id)
        for key, value in fields.items():
            setattr(group, key, value)
        return group

    def delete(self, group_id: int):
        """
        Deletes a group from storage.

        Args:
            group_id (int) id of the group to delete

        Returns:
            None

        Exceptions:
            None
        """
        if group_id in self.groups:
            del self.groups[group_id]


@pytest.fixture
def mock_repo():
    """
    Creates a new in-memory mock group repository.

    Args:
        None

    Returns:
        MockGroupRepository repository instance

    Exceptions:
        None
    """
    return MockGroupRepository()


@pytest.fixture
def group_service(mock_repo):
    """
    Creates a GroupService instance for testing.

    Args:
        mock_repo (MockGroupRepository) repository instance

    Returns:
        GroupService service instance

    Exceptions:
        None
    """
    return GroupService(mock_repo)


@pytest.fixture
def sample_group_data():
    """
    Provides base data for creating a group in tests.

    Args:
        None

    Returns:
        dict sample fields for a new group

    Exceptions:
        None
    """
    return {"name": "Trip Crew", "description": "Friends going on a trip"}


def test_create_group(group_service, mock_repo, sample_group_data):
    """
    Verifies that group creation works correctly.

    Args:
        group_service (GroupService) service under test
        mock_repo (MockGroupRepository) storage
        sample_group_data (dict) input fields

    Returns:
        None

    Exceptions:
        AssertionError on mismatch
    """
    data = GroupCreate(**sample_group_data)
    group = group_service.create_group(data)

    assert group.id == 1
    assert group.name == "Trip Crew"
    assert hasattr(group, "invitation_code")
    assert len(group.invitation_code) == 6
    assert len(mock_repo.groups) == 1


def test_get_group_by_id_success(group_service, mock_repo, sample_group_data):
    """
    Ensures retrieving a group by id works.

    Args:
        group_service (GroupService) service
        mock_repo (MockGroupRepository) repo
        sample_group_data (dict) initial fields

    Returns:
        None

    Exceptions:
        AssertionError when returned values differ
    """
    created = group_service.create_group(GroupCreate(**sample_group_data))
    retrieved = group_service.get_group_by_id(created.id)

    assert retrieved.id == created.id
    assert retrieved.name == created.name


def test_get_group_by_id_not_found(group_service):
    """
    Ensures requesting a missing group raises an exception.

    Args:
        group_service (GroupService) service

    Returns:
        None

    Exceptions:
        NoResultFound expected
    """
    with pytest.raises(NoResultFound):
        group_service.get_group_by_id(999)


def test_get_group_by_name_success(group_service, mock_repo, sample_group_data):
    """
    Tests retrieving a group by name.

    Args:
        group_service (GroupService) service
        mock_repo (MockGroupRepository) repo
        sample_group_data (dict) base group data

    Returns:
        None

    Exceptions:
        AssertionError if wrong value returned
    """
    created = group_service.create_group(GroupCreate(**sample_group_data))
    retrieved = group_service.get_group_by_name("Trip Crew")

    assert retrieved.id == created.id
    assert retrieved.name == "Trip Crew"


def test_get_group_by_name_not_found(group_service):
    """
    Ensures requesting a missing name raises an exception.

    Args:
        group_service (GroupService) service

    Returns:
        None

    Exceptions:
        NoResultFound expected
    """
    with pytest.raises(NoResultFound):
        group_service.get_group_by_name("Missing Group")


def test_get_all_groups(group_service, mock_repo):
    """
    Tests retrieving all groups with default pagination.

    Args:
        group_service (GroupService) service
        mock_repo (MockGroupRepository) repo

    Returns:
        None

    Exceptions:
        AssertionError on incorrect output size or order
    """
    group_service.create_group(GroupCreate(name="G1"))
    group_service.create_group(GroupCreate(name="G2"))

    result = group_service.get_all_groups()

    assert len(result) == 2
    assert result[0].name == "G1"
    assert result[1].name == "G2"


def test_update_group_success(group_service, mock_repo, sample_group_data):
    """
    Tests successful group update.

    Args:
        group_service (GroupService) service
        mock_repo (MockGroupRepository) repo
        sample_group_data (dict) initial fields

    Returns:
        None

    Exceptions:
        AssertionError when update fails
    """
    created = group_service.create_group(GroupCreate(**sample_group_data))

    update_data = GroupUpdate(description="New description")
    updated = group_service.update_group(created.id, update_data)

    assert updated.description == "New description"
    assert mock_repo.groups[created.id].description == "New description"


def test_update_group_not_found(group_service):
    """
    Verifies updating a missing group triggers an error.

    Args:
        group_service (GroupService) service

    Returns:
        None

    Exceptions:
        NoResultFound expected
    """
    with pytest.raises(NoResultFound):
        group_service.update_group(999, GroupUpdate(description="x"))


def test_update_group_no_fields(group_service, mock_repo, sample_group_data):
    """
    Ensures empty update request raises validation error.

    Args:
        group_service (GroupService) service
        mock_repo (MockGroupRepository) repo
        sample_group_data (dict) initial fields

    Returns:
        None

    Exceptions:
        ValueError expected when no fields provided
    """
    created = group_service.create_group(GroupCreate(**sample_group_data))

    with pytest.raises(ValueError):
        group_service.update_group(created.id, GroupUpdate())


def test_delete_group_success(group_service, mock_repo, sample_group_data):
    """
    Verifies group deletion works.

    Args:
        group_service (GroupService) service
        mock_repo (MockGroupRepository) repo
        sample_group_data (dict) fields for creation

    Returns:
        None

    Exceptions:
        AssertionError on incorrect repo state
    """
    created = group_service.create_group(GroupCreate(**sample_group_data))
    assert len(mock_repo.groups) == 1

    group_service.delete_group(created.id)

    assert len(mock_repo.groups) == 0


def test_delete_group_not_found(group_service):
    """
    Ensures deleting a missing group raises an error.

    Args:
        group_service (GroupService) service

    Returns:
        None

    Exceptions:
        NoResultFound expected
    """
    with pytest.raises(NoResultFound):
        group_service.delete_group(123)