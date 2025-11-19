from datetime import datetime, timedelta
from operator import attrgetter
from typing import List, Optional

import pytest
from models.expense import Expense
from schemas.expense import ExpenseCreate, ExpenseUpdate
from services.expense_service import ExpenseService
from sqlalchemy.exc import NoResultFound


class MockExpenseRepository:
    """
    Provides an in-memory mock implementation of the expense repository for testing.

    Args:
        None

    Returns:
        MockExpenseRepository instance with stored expenses

    Exceptions:
        None
    """

    def __init__(self):
        """
        Initializes storage for mock expenses and group memberships.

        Args:
            None

        Returns:
            None

        Exceptions:
            None
        """
        self.expenses = {}
        self.counter = 1
        self.users_groups = {}  # Stores user_id: [group_id1, group_id2]

    def add(self, expense: Expense):
        """
        Adds an expense to the in-memory store.

        Args:
            expense (Expense) expense object to store

        Returns:
            Expense stored expense with assigned id

        Exceptions:
            None
        """
        expense.id = self.counter
        if not expense.created_at:
            expense.created_at = datetime.utcnow() + timedelta(seconds=self.counter)
        self.expenses[self.counter] = expense
        self.counter += 1
        return expense

    def get_by_id(self, expense_id: int):
        """
        Retrieves an expense by its id.

        Args:
            expense_id (int) identifier of the expense

        Returns:
            Expense or None if not found

        Exceptions:
            None
        """
        return self.expenses.get(expense_id)

    def get_all(
        self,
        offset: int = 0,
        limit: int = 100,
        sort_by: str = "created_at",
        order: str = "desc",
        min_price: Optional[float] = None,
        max_price: Optional[float] = None,
        date_from: Optional[datetime] = None,
        date_to: Optional[datetime] = None,
        category: Optional[str] = None
    ):
        """
        Returns all stored expenses with filtering and sorting.

        Args:
            offset (int) starting index
            limit (int) maximum number of items
            sort_by (str) field to sort by
            order (str) sort direction
            min_price (float|None) minimum allowed amount
            max_price (float|None) maximum allowed amount
            date_from (datetime|None) filter lower date bound
            date_to (datetime|None) filter upper date bound
            category (str|None) category filter

        Returns:
            list filtered and sorted expenses

        Exceptions:
            None
        """
        expenses = list(self.expenses.values())

        filtered = []
        for e in expenses:
            if min_price is not None and e.amount < min_price:
                continue
            if max_price is not None and e.amount > max_price:
                continue
            if date_from is not None and e.created_at < date_from:
                continue
            if date_to is not None and e.created_at > date_to:
                continue
            if category is not None and e.category != category:
                continue
            filtered.append(e)

        filtered.sort(key=attrgetter(sort_by), reverse=(order.lower() == "desc"))
        return filtered[offset: offset + limit]

    def get_by_user(
        self,
        user_id: int,
        offset: int = 0,
        limit: int = 100,
        sort_by: str = "created_at",
        order: str = "desc",
        min_price: Optional[float] = None,
        max_price: Optional[float] = None,
        date_from: Optional[datetime] = None,
        date_to: Optional[datetime] = None,
        category: Optional[str] = None,
        group_ids: Optional[List[int]] = None
    ):
        """
        Returns expenses belonging to a user or their groups with filters applied.

        Args:
            user_id (int) id of the user
            offset (int) starting index
            limit (int) maximum number of results
            sort_by (str) field used for sorting
            order (str) sorting direction
            min_price (float|None) minimum amount
            max_price (float|None) maximum amount
            date_from (datetime|None) lower date bound
            date_to (datetime|None) upper date bound
            category (str|None) category filter
            group_ids (list[int]|None) groups to filter by

        Returns:
            list filtered expenses belonging to user or user's groups

        Exceptions:
            None
        """
        user_group_ids = self.users_groups.get(user_id, [])
        all_expenses = list(self.expenses.values())
        potential_expenses = []

        if group_ids:
            allowed_group_ids = [gid for gid in group_ids if gid in user_group_ids]
            for e in all_expenses:
                if e.group_id in allowed_group_ids:
                    potential_expenses.append(e)
        else:
            for e in all_expenses:
                if e.user_id == user_id or e.group_id in user_group_ids:
                    potential_expenses.append(e)

        filtered = []
        for e in potential_expenses:
            if min_price is not None and e.amount < min_price:
                continue
            if max_price is not None and e.amount > max_price:
                continue
            if date_from is not None and e.created_at < date_from:
                continue
            if date_to is not None and e.created_at > date_to:
                continue
            if category is not None and e.category != category:
                continue
            filtered.append(e)

        filtered.sort(key=attrgetter(sort_by), reverse=(order.lower() == "desc"))
        return filtered[offset: offset + limit]

    def get_by_group(
        self,
        group_id: int,
        offset: int = 0,
        limit: int = 100,
        sort_by: str = "created_at",
        order: str = "desc",
        min_price: Optional[float] = None,
        max_price: Optional[float] = None,
        date_from: Optional[datetime] = None,
        date_to: Optional[datetime] = None,
        category: Optional[str] = None
    ):
        """
        Returns expenses belonging to a specific group with optional filters.

        Args:
            group_id (int) id of the group
            offset (int) starting index
            limit (int) limit of results
            sort_by (str) field used for sorting
            order (str) direction asc or desc
            min_price (float|None) lower amount bound
            max_price (float|None) upper amount bound
            date_from (datetime|None) filter lower date bound
            date_to (datetime|None) filter upper date bound
            category (str|None) category filter

        Returns:
            list expenses belonging to the group

        Exceptions:
            None
        """
        expenses = [e for e in self.expenses.values() if e.group_id == group_id]

        filtered = []
        for e in expenses:
            if min_price is not None and e.amount < min_price:
                continue
            if max_price is not None and e.amount > max_price:
                continue
            if date_from is not None and e.created_at < date_from:
                continue
            if date_to is not None and e.created_at > date_to:
                continue
            if category is not None and e.category != category:
                continue
            filtered.append(e)

        filtered.sort(key=attrgetter(sort_by), reverse=(order.lower() == "desc"))
        return filtered[offset: offset + limit]

    def update(self, expense_id: int, fields: dict):
        """
        Updates fields of an existing expense.

        Args:
            expense_id (int) id of the expense
            fields (dict) changed key-value pairs

        Returns:
            Expense updated object

        Exceptions:
            NoResultFound raised when expense does not exist
        """
        exp = self.expenses.get(expense_id)
        if not exp:
            raise NoResultFound
        for key, value in fields.items():
            setattr(exp, key, value)
        return exp

    def delete(self, expense_id: int):
        """
        Removes an expense from storage.

        Args:
            expense_id (int) identifier of the expense

        Returns:
            None

        Exceptions:
            NoResultFound raised when expense does not exist
        """
        if expense_id not in self.expenses:
            raise NoResultFound
        del self.expenses[expense_id]


@pytest.fixture
def mock_repo():
    """
    Provides a fresh mock repository instance.

    Args:
        None

    Returns:
        MockExpenseRepository new mock repository

    Exceptions:
        None
    """
    return MockExpenseRepository()


@pytest.fixture
def expense_service(mock_repo):
    """
    Provides an ExpenseService using the mock repository.

    Args:
        mock_repo (MockExpenseRepository) in-memory storage

    Returns:
        ExpenseService service instance

    Exceptions:
        None
    """
    return ExpenseService(repository=mock_repo)


@pytest.fixture
def sample_expense_data():
    """
    Provides base expense data for tests.

    Args:
        None

    Returns:
        dict expense fields

    Exceptions:
        None
    """
    return {
        "title": "Lunch",
        "category": "Food",
        "amount": 12.5,
        "user_id": 1,
        "group_id": None
    }


@pytest.fixture
def populated_service(expense_service):
    """
    Populates the mock service with multiple expenses for filter testing.

    Args:
        expense_service (ExpenseService) service used for populating

    Returns:
        tuple service instance and base_time used for timestamps

    Exceptions:
        None
    """
    base_time = datetime.utcnow()

    exp1 = ExpenseCreate(title="Coffee", category="Food", amount=5, user_id=1)
    exp2 = ExpenseCreate(title="Movie", category="Entertainment", amount=15, user_id=1)
    exp3 = ExpenseCreate(title="Groceries", category="Food", amount=50, user_id=2)
    exp4 = ExpenseCreate(title="Dinner", category="Food", amount=30, user_id=1)
    exp5 = ExpenseCreate(title="Shared Ride", category="Transport", amount=20, user_id=5, group_id=10)
    exp6 = ExpenseCreate(title="Team Lunch", category="Food", amount=100, user_id=5, group_id=11)
    exp7 = ExpenseCreate(title="Office Supplies", category="Work", amount=75, user_id=5, group_id=10)

    service = expense_service
    service.create_expense(exp1).created_at = base_time - timedelta(days=5)
    service.create_expense(exp2).created_at = base_time - timedelta(days=3)
    service.create_expense(exp3).created_at = base_time - timedelta(days=2)
    service.create_expense(exp4).created_at = base_time - timedelta(days=1)
    service.create_expense(exp5).created_at = base_time - timedelta(days=6)
    service.create_expense(exp6).created_at = base_time - timedelta(days=0)
    service.create_expense(exp7).created_at = base_time - timedelta(days=2)

    service.repository.expenses[1].created_at = base_time - timedelta(days=5)
    service.repository.expenses[2].created_at = base_time - timedelta(days=3)
    service.repository.expenses[3].created_at = base_time - timedelta(days=2)
    service.repository.expenses[4].created_at = base_time - timedelta(days=1)
    service.repository.expenses[5].created_at = base_time - timedelta(days=6)
    service.repository.expenses[6].created_at = base_time - timedelta(days=0)
    service.repository.expenses[7].created_at = base_time - timedelta(days=2)

    service.repository.users_groups = {
        1: [10],
        2: [11]
    }

    return service, base_time


# --------------------------------------------------
# ------------------- TESTS ------------------------
# --------------------------------------------------

def test_create_expense(expense_service, mock_repo, sample_expense_data):
    """
    Tests creating an expense.

    Args:
        expense_service (ExpenseService) service under test
        mock_repo (MockExpenseRepository) storage
        sample_expense_data (dict) sample data

    Returns:
        None

    Exceptions:
        AssertionError raised on test failure
    """
    data = ExpenseCreate(**sample_expense_data)
    exp = expense_service.create_expense(data)
    assert len(mock_repo.expenses) == 1
    assert exp.title == "Lunch"
    assert exp.amount == 12.5
    assert exp.user_id == 1


def test_get_expense_by_id_success(expense_service, mock_repo, sample_expense_data):
    """
    Tests retrieving an existing expense by id.

    Args:
        expense_service (ExpenseService) service under test
        mock_repo (MockExpenseRepository) storage
        sample_expense_data (dict) initial expense

    Returns:
        None

    Exceptions:
        AssertionError on mismatch
    """
    data = ExpenseCreate(**sample_expense_data)
    expense_service.create_expense(data)
    expense = expense_service.get_expense_by_id(1)
    assert expense.id == 1


def test_get_expense_by_id_not_found(expense_service):
    """
    Tests requesting a non-existent expense.

    Args:
        expense_service (ExpenseService) service under test

    Returns:
        None

    Exceptions:
        NoResultFound expected
    """
    with pytest.raises(NoResultFound):
        expense_service.get_expense_by_id(999)


def test_get_all_expenses_sorted(expense_service, mock_repo):
    """
    Tests sorting of all expenses by field.

    Args:
        expense_service (ExpenseService) service under test
        mock_repo (MockExpenseRepository) storage

    Returns:
        None

    Exceptions:
        AssertionError on incorrect ordering
    """
    titles = ["B", "A", "C"]
    for t in titles:
        expense_service.create_expense(ExpenseCreate(title=t, category="Cat", amount=10, user_id=1))
    result = expense_service.get_all_expenses(sort_by="title", order="asc")
    assert [e.title for e in result] == ["A", "B", "C"]
    result = expense_service.get_all_expenses(sort_by="title", order="desc")
    assert [e.title for e in result] == ["C", "B", "A"]


def test_get_user_expenses_sorted(expense_service, mock_repo):
    """
    Tests sorting user-specific expenses.

    Args:
        expense_service (ExpenseService) service
        mock_repo (MockExpenseRepository) repo

    Returns:
        None

    Exceptions:
        AssertionError on mismatch
    """
    titles = ["X", "M", "A"]
    for t in titles:
        expense_service.create_expense(ExpenseCreate(title=t, category="Food", amount=5, user_id=42))
    result = expense_service.get_user_expenses(user_id=42, sort_by="title", order="asc")
    assert [e.title for e in result] == ["A", "M", "X"]


def test_get_group_expenses_sorted(expense_service, mock_repo):
    """
    Tests sorting of group expenses.

    Args:
        expense_service (ExpenseService) service
        mock_repo (MockExpenseRepository) repo

    Returns:
        None

    Exceptions:
        AssertionError on mismatch
    """
    titles = ["G", "C", "E"]
    for t in titles:
        expense_service.create_expense(ExpenseCreate(title=t, category="Trip", amount=20, user_id=1, group_id=7))
    result = expense_service.get_group_expenses(group_id=7, sort_by="title", order="desc")
    assert [e.title for e in result] == ["G", "E", "C"]


def test_update_expense_success(expense_service, mock_repo, sample_expense_data):
    """
    Tests updating a stored expense.

    Args:
        expense_service (ExpenseService) service
        mock_repo (MockExpenseRepository) repo
        sample_expense_data (dict) initial data

    Returns:
        None

    Exceptions:
        AssertionError when update fails
    """
    data = ExpenseCreate(**sample_expense_data)
    expense_service.create_expense(data)
    update_data = ExpenseUpdate(title="Updated Lunch", user_id=1)
    updated = expense_service.update_expense(1, update_data)
    assert updated.title == "Updated Lunch"


def test_delete_expense_success(expense_service, mock_repo, sample_expense_data):
    """
    Tests successful deletion of an expense.

    Args:
        expense_service (ExpenseService) service
        mock_repo (MockExpenseRepository) storage
        sample_expense_data (dict) initial data

    Returns:
        None

    Exceptions:
        AssertionError on incorrect repo state
    """
    data = ExpenseCreate(**sample_expense_data)
    expense_service.create_expense(data)
    assert len(mock_repo.expenses) == 1
    expense_service.delete_expense(1)
    assert len(mock_repo.expenses) == 0


def test_delete_expense_not_found(expense_service):
    """
    Tests deletion attempt on non-existent expense.

    Args:
        expense_service (ExpenseService) service

    Returns:
        None

    Exceptions:
        NoResultFound expected
    """
    with pytest.raises(NoResultFound):
        expense_service.delete_expense(123)


def test_get_all_expenses_filtered(populated_service):
    """
    Tests all filter combinations on all-expenses endpoint.

    Args:
        populated_service (tuple) service and base_time

    Returns:
        None

    Exceptions:
        AssertionError on incorrect filtered results
    """
    service, base_time = populated_service

    res_min = service.get_all_expenses(min_price=20)
    assert {e.title for e in res_min} == {"Groceries", "Dinner", "Shared Ride", "Team Lunch", "Office Supplies"}

    res_max = service.get_all_expenses(max_price=10)
    assert {e.title for e in res_max} == {"Coffee"}

    res_range = service.get_all_expenses(min_price=10, max_price=40)
    assert {e.title for e in res_range} == {"Movie", "Dinner", "Shared Ride"}

    res_cat = service.get_all_expenses(category="Food")
    assert {e.title for e in res_cat} == {"Coffee", "Groceries", "Dinner", "Team Lunch"}

    res_date_from = service.get_all_expenses(date_from=base_time - timedelta(days=2.5))
    assert {e.title for e in res_date_from} == {"Groceries", "Dinner", "Team Lunch", "Office Supplies"}

    res_date_to = service.get_all_expenses(date_to=base_time - timedelta(days=4))
    assert {e.title for e in res_date_to} == {"Coffee", "Shared Ride"}

    res_combo = service.get_all_expenses(category="Food", min_price=40)
    assert {e.title for e in res_combo} == {"Groceries", "Team Lunch"}


def test_get_user_expenses_filtered(populated_service):
    """
    Tests filtering user-specific expenses.

    Args:
        populated_service (tuple) service and time

    Returns:
        None

    Exceptions:
        AssertionError on incorrect results
    """
    service, base_time = populated_service

    all_user1 = service.get_user_expenses(user_id=1)
    assert {e.title for e in all_user1} == {"Coffee", "Movie", "Dinner", "Shared Ride", "Office Supplies"}

    res_cat = service.get_user_expenses(user_id=1, category="Food")
    assert {e.title for e in res_cat} == {"Coffee", "Dinner"}

    res_price = service.get_user_expenses(user_id=1, min_price=20)
    assert {e.title for e in res_price} == {"Dinner", "Shared Ride", "Office Supplies"}

    res_date = service.get_user_expenses(user_id=1, date_from=base_time - timedelta(days=4))
    assert {e.title for e in res_date} == {"Movie", "Dinner", "Office Supplies"}

    res_none = service.get_user_expenses(user_id=1, category="Entertainment", max_price=10)
    assert len(res_none) == 0

    res_user2 = service.get_user_expenses(user_id=2, category="Food")
    assert {e.title for e in res_user2} == {"Groceries", "Team Lunch"}


def test_get_user_expenses_group_filter(populated_service):
    """
    Tests filtering by group_ids logic for users.

    Args:
        populated_service (tuple) service and time

    Returns:
        None

    Exceptions:
        AssertionError on incorrect group filtering
    """
    service, base_time = populated_service

    res_user1_group10 = service.get_user_expenses(user_id=1, group_ids=[10])
    assert {e.title for e in res_user1_group10} == {"Shared Ride", "Office Supplies"}

    res_user1_group11 = service.get_user_expenses(user_id=1, group_ids=[11])
    assert len(res_user1_group11) == 0

    res_user1_both = service.get_user_expenses(user_id=1, group_ids=[10, 11])
    assert {e.title for e in res_user1_both} == {"Shared Ride", "Office Supplies"}

    res_user2_group11 = service.get_user_expenses(user_id=2, group_ids=[11])
    assert {e.title for e in res_user2_group11} == {"Team Lunch"}

    res_user2_group10 = service.get_user_expenses(user_id=2, group_ids=[10])
    assert len(res_user2_group10) == 0


def test_get_group_expenses_filtered(populated_service):
    """
    Tests filtering of group-specific expenses.

    Args:
        populated_service (tuple) service and time

    Returns:
        None

    Exceptions:
        AssertionError on incorrect group filtering
    """
    service, base_time = populated_service

    res_min = service.get_group_expenses(group_id=10, min_price=50)
    assert {e.title for e in res_min} == {"Office Supplies"}

    res_max = service.get_group_expenses(group_id=10, max_price=30)
    assert {e.title for e in res_max} == {"Shared Ride"}

    res_cat = service.get_group_expenses(group_id=10, category="Work")
    assert {e.title for e in res_cat} == {"Office Supplies"}

    res_date = service.get_group_expenses(group_id=10, date_from=base_time - timedelta(days=3))
    assert {e.title for e in res_date} == {"Office Supplies"}

    res_combo = service.get_group_expenses(group_id=10, category="Transport", min_price=30)
    assert len(res_combo) == 0
