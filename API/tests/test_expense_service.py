from datetime import datetime, timedelta
from operator import attrgetter
from typing import List, Optional

import pytest
from models.expense import Expense
from schemas.expense import ExpenseCreate, ExpenseUpdate
from services.expense_service import ExpenseService
from sqlalchemy.exc import NoResultFound


class MockExpenseRepository:
    def __init__(self):
        self.expenses = {}
        self.counter = 1
        self.users_groups = {}  # Stores user_id: [group_id1, group_id2]

    def add(self, expense: Expense):
        expense.id = self.counter
        if not expense.created_at:
             # Set a unique timestamp so sorting works predictably
            expense.created_at = datetime.utcnow() + timedelta(seconds=self.counter)
        self.expenses[self.counter] = expense
        self.counter += 1
        return expense

    def get_by_id(self, expense_id: int):
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
        expenses = list(self.expenses.values())
        
        # Apply filters
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
        user_group_ids = self.users_groups.get(user_id, [])
        all_expenses = list(self.expenses.values())
        potential_expenses = []

        if group_ids:
            # Filter by specific groups, but only those the user is in
            allowed_group_ids = [gid for gid in group_ids if gid in user_group_ids]
            for e in all_expenses:
                if e.group_id in allowed_group_ids:
                    potential_expenses.append(e)
        else:
            # No group filter: get personal expenses OR expenses from any of user's groups
            for e in all_expenses:
                if e.user_id == user_id or e.group_id in user_group_ids:
                    potential_expenses.append(e)
        
        # Apply other filters
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

    # --- THIS IS THE MODIFIED METHOD ---
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
        expenses = [e for e in self.expenses.values() if e.group_id == group_id]
        
        # Apply filters
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
    # --- END MODIFIED METHOD ---

    def update(self, expense_id: int, fields: dict):
        exp = self.expenses.get(expense_id)
        if not exp:
            raise NoResultFound
        for key, value in fields.items():
            setattr(exp, key, value)
        return exp

    def delete(self, expense_id: int):
        if expense_id not in self.expenses:
            raise NoResultFound
        del self.expenses[expense_id]

@pytest.fixture
def mock_repo():
    return MockExpenseRepository()

@pytest.fixture
def expense_service(mock_repo):
    return ExpenseService(repository=mock_repo)

@pytest.fixture
def sample_expense_data():
    return {
        "title": "Lunch",
        "category": "Food",
        "amount": 12.5,
        "user_id": 1,
        "group_id": None
    }

# Fixture to populate the service with data for filter tests
@pytest.fixture
def populated_service(expense_service):
    base_time = datetime.utcnow()
    
    # Create expenses
    exp1 = ExpenseCreate(title="Coffee", category="Food", amount=5, user_id=1)
    exp2 = ExpenseCreate(title="Movie", category="Entertainment", amount=15, user_id=1)
    exp3 = ExpenseCreate(title="Groceries", category="Food", amount=50, user_id=2)
    exp4 = ExpenseCreate(title="Dinner", category="Food", amount=30, user_id=1)
    # --- Group Expenses ---
    exp5 = ExpenseCreate(title="Shared Ride", category="Transport", amount=20, user_id=5, group_id=10)
    exp6 = ExpenseCreate(title="Team Lunch", category="Food", amount=100, user_id=5, group_id=11)
    exp7 = ExpenseCreate(title="Office Supplies", category="Work", amount=75, user_id=5, group_id=10) # Added for group filter test

    # Add and manually set created_at for predictable date filtering
    service = expense_service
    service.create_expense(exp1).created_at = base_time - timedelta(days=5) # id 1
    service.create_expense(exp2).created_at = base_time - timedelta(days=3) # id 2
    service.create_expense(exp3).created_at = base_time - timedelta(days=2) # id 3
    service.create_expense(exp4).created_at = base_time - timedelta(days=1) # id 4
    service.create_expense(exp5).created_at = base_time - timedelta(days=6) # id 5
    service.create_expense(exp6).created_at = base_time - timedelta(days=0) # id 6
    service.create_expense(exp7).created_at = base_time - timedelta(days=2) # id 7
    
    # Update mock repo entries with the new timestamps
    service.repository.expenses[1].created_at = base_time - timedelta(days=5)
    service.repository.expenses[2].created_at = base_time - timedelta(days=3)
    service.repository.expenses[3].created_at = base_time - timedelta(days=2)
    service.repository.expenses[4].created_at = base_time - timedelta(days=1)
    service.repository.expenses[5].created_at = base_time - timedelta(days=6)
    service.repository.expenses[6].created_at = base_time - timedelta(days=0)
    service.repository.expenses[7].created_at = base_time - timedelta(days=2)

    # Set up group memberships
    service.repository.users_groups = {
        1: [10], # User 1 is in group 10
        2: [11]  # User 2 is in group 11
    }

    return service, base_time

# --- Original Tests (Still valid) ---

def test_create_expense(expense_service, mock_repo, sample_expense_data):
    data = ExpenseCreate(**sample_expense_data)
    exp = expense_service.create_expense(data)
    assert len(mock_repo.expenses) == 1
    assert exp.title == "Lunch"
    assert exp.amount == 12.5
    assert exp.user_id == 1

def test_get_expense_by_id_success(expense_service, mock_repo, sample_expense_data):
    data = ExpenseCreate(**sample_expense_data)
    expense_service.create_expense(data)
    expense = expense_service.get_expense_by_id(1)
    assert expense.id == 1

def test_get_expense_by_id_not_found(expense_service):
    with pytest.raises(NoResultFound):
        expense_service.get_expense_by_id(999)

def test_get_all_expenses_sorted(expense_service, mock_repo):
    # create expenses with different titles
    titles = ["B", "A", "C"]
    for t in titles:
        expense_service.create_expense(ExpenseCreate(title=t, category="Cat", amount=10, user_id=1))
    # sort by title ascending
    result = expense_service.get_all_expenses(sort_by="title", order="asc")
    assert [e.title for e in result] == ["A", "B", "C"]
    # sort by title descending
    result = expense_service.get_all_expenses(sort_by="title", order="desc")
    assert [e.title for e in result] == ["C", "B", "A"]

def test_get_user_expenses_sorted(expense_service, mock_repo):
    titles = ["X", "M", "A"]
    for t in titles:
        expense_service.create_expense(ExpenseCreate(title=t, category="Food", amount=5, user_id=42))
    result = expense_service.get_user_expenses(user_id=42, sort_by="title", order="asc")
    assert [e.title for e in result] == ["A", "M", "X"]

def test_get_group_expenses_sorted(expense_service, mock_repo):
    titles = ["G", "C", "E"]
    for t in titles:
        expense_service.create_expense(ExpenseCreate(title=t, category="Trip", amount=20, user_id=1, group_id=7))
    # This call now correctly passes the default values for the new arguments
    result = expense_service.get_group_expenses(group_id=7, sort_by="title", order="desc")
    assert [e.title for e in result] == ["G", "E", "C"]

def test_update_expense_success(expense_service, mock_repo, sample_expense_data):
    data = ExpenseCreate(**sample_expense_data)
    expense_service.create_expense(data)
    update_data = ExpenseUpdate(title="Updated Lunch", user_id=1)
    updated = expense_service.update_expense(1, update_data)
    assert updated.title == "Updated Lunch"

def test_delete_expense_success(expense_service, mock_repo, sample_expense_data):
    data = ExpenseCreate(**sample_expense_data)
    expense_service.create_expense(data)
    assert len(mock_repo.expenses) == 1
    expense_service.delete_expense(1)
    assert len(mock_repo.expenses) == 0

def test_delete_expense_not_found(expense_service):
    with pytest.raises(NoResultFound):
        expense_service.delete_expense(123)

# --- New Tests for Filtering ---

def test_get_all_expenses_filtered(populated_service):
    service, base_time = populated_service
    
    # Price filters
    res_min = service.get_all_expenses(min_price=20)
    assert {e.title for e in res_min} == {"Groceries", "Dinner", "Shared Ride", "Team Lunch", "Office Supplies"}
    
    res_max = service.get_all_expenses(max_price=10)
    assert {e.title for e in res_max} == {"Coffee"}
    
    res_range = service.get_all_expenses(min_price=10, max_price=40)
    assert {e.title for e in res_range} == {"Movie", "Dinner", "Shared Ride"}
    
    # Category filter
    res_cat = service.get_all_expenses(category="Food")
    assert {e.title for e in res_cat} == {"Coffee", "Groceries", "Dinner", "Team Lunch"}
    
    # Date filter
    res_date_from = service.get_all_expenses(date_from=base_time - timedelta(days=2.5))
    assert {e.title for e in res_date_from} == {"Groceries", "Dinner", "Team Lunch", "Office Supplies"}
    
    res_date_to = service.get_all_expenses(date_to=base_time - timedelta(days=4))
    assert {e.title for e in res_date_to} == {"Coffee", "Shared Ride"}
    
    # Combined filter
    res_combo = service.get_all_expenses(category="Food", min_price=40)
    assert {e.title for e in res_combo} == {"Groceries", "Team Lunch"}

def test_get_user_expenses_filtered(populated_service):
    service, base_time = populated_service
    
    # User 1 is in group [10].
    # User 1 personal: Coffee (5), Movie (15), Dinner (30)
    # Group 10: Shared Ride (20), Office Supplies (75)
    # User 2 personal: Groceries (50)
    # Group 11: Team Lunch (100)
    
    # Get all for user 1 (should be personal + group 10)
    all_user1 = service.get_user_expenses(user_id=1)
    assert {e.title for e in all_user1} == {"Coffee", "Movie", "Dinner", "Shared Ride", "Office Supplies"}
    assert len(all_user1) == 5

    # Filter user 1 by category
    res_cat = service.get_user_expenses(user_id=1, category="Food")
    assert {e.title for e in res_cat} == {"Coffee", "Dinner"}
    
    # Filter user 1 by price
    res_price = service.get_user_expenses(user_id=1, min_price=20)
    assert {e.title for e in res_price} == {"Dinner", "Shared Ride", "Office Supplies"}
    
    # Filter user 1 by date
    res_date = service.get_user_expenses(user_id=1, date_from=base_time - timedelta(days=4))
    assert {e.title for e in res_date} == {"Movie", "Dinner", "Office Supplies"}

    # Filter user 1 but get no results (should be none)
    res_none = service.get_user_expenses(user_id=1, category="Entertainment", max_price=10)
    assert len(res_none) == 0

    # Check user 2 (should be personal + group 11)
    res_user2 = service.get_user_expenses(user_id=2, category="Food")
    assert {e.title for e in res_user2} == {"Groceries", "Team Lunch"}


# --- ADDED NEW TEST for group_ids filter ---

def test_get_user_expenses_group_filter(populated_service):
    service, base_time = populated_service

    # User 1 is in group [10].
    # User 2 is in group [11].
    # Expense 5 ("Shared Ride"), 7 ("Office Supplies") are in group 10.
    # Expense 6 ("Team Lunch") is in group 11.

    # Ask for user 1, filtering by group 10
    res_user1_group10 = service.get_user_expenses(user_id=1, group_ids=[10])
    assert {e.title for e in res_user1_group10} == {"Shared Ride", "Office Supplies"}

    # Ask for user 1, filtering by group 11 (which they are not in)
    res_user1_group11 = service.get_user_expenses(user_id=1, group_ids=[11])
    assert len(res_user1_group11) == 0

    # Ask for user 1, filtering by both (should only get 10)
    res_user1_both = service.get_user_expenses(user_id=1, group_ids=[10, 11])
    assert {e.title for e in res_user1_both} == {"Shared Ride", "Office Supplies"}

    # Ask for user 2, filtering by group 11
    res_user2_group11 = service.get_user_expenses(user_id=2, group_ids=[11])
    assert {e.title for e in res_user2_group11} == {"Team Lunch"}
    
    # Ask for user 2, filtering by group 10 (which they are not in)
    res_user2_group10 = service.get_user_expenses(user_id=2, group_ids=[10])
    assert len(res_user2_group10) == 0


# --- ADDED NEW TEST for get_group_expenses filtering ---
def test_get_group_expenses_filtered(populated_service):
    service, base_time = populated_service
    
    # Group 10 has:
    # - "Shared Ride" (20, "Transport", day -6)
    # - "Office Supplies" (75, "Work", day -2)
    
    # Filter by price
    res_min = service.get_group_expenses(group_id=10, min_price=50)
    assert {e.title for e in res_min} == {"Office Supplies"}
    
    res_max = service.get_group_expenses(group_id=10, max_price=30)
    assert {e.title for e in res_max} == {"Shared Ride"}
    
    # Filter by category
    res_cat = service.get_group_expenses(group_id=10, category="Work")
    assert {e.title for e in res_cat} == {"Office Supplies"}
    
    # Filter by date
    res_date = service.get_group_expenses(group_id=10, date_from=base_time - timedelta(days=3))
    assert {e.title for e in res_date} == {"Office Supplies"}
    
    # Combined
    res_combo = service.get_group_expenses(group_id=10, category="Transport", min_price=30)
    assert len(res_combo) == 0 # "Shared Ride" is only 20