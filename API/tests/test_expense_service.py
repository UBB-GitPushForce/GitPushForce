from datetime import datetime, timedelta
from operator import attrgetter
from typing import Optional

import pytest
from models.expense import Expense
from schemas.expense import ExpenseCreate, ExpenseUpdate
from services.expense_service import ExpenseService
from sqlalchemy.exc import NoResultFound


class MockExpenseRepository:
    def __init__(self):
        self.expenses = {}
        self.counter = 1

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
        category: Optional[str] = None
    ):
        expenses = [e for e in self.expenses.values() if e.user_id == user_id]
        
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

    def get_by_group(self, group_id: int, offset: int = 0, limit: int = 100, sort_by: str = "created_at", order: str = "desc"):
        expenses = [e for e in self.expenses.values() if e.group_id == group_id]
        expenses.sort(key=attrgetter(sort_by), reverse=(order.lower() == "desc"))
        return expenses[offset: offset + limit]

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
    
    # Add and manually set created_at for predictable date filtering
    service = expense_service
    service.create_expense(exp1).created_at = base_time - timedelta(days=5) # id 1
    service.create_expense(exp2).created_at = base_time - timedelta(days=3) # id 2
    service.create_expense(exp3).created_at = base_time - timedelta(days=2) # id 3
    service.create_expense(exp4).created_at = base_time - timedelta(days=1) # id 4
    
    # Update mock repo entries with the new timestamps
    service.repository.expenses[1].created_at = base_time - timedelta(days=5)
    service.repository.expenses[2].created_at = base_time - timedelta(days=3)
    service.repository.expenses[3].created_at = base_time - timedelta(days=2)
    service.repository.expenses[4].created_at = base_time - timedelta(days=1)

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
        expense_service.create_expense(ExpenseCreate(title=t, category="Trip", amount=20, user_id=None, group_id=7))
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
    assert {e.title for e in res_min} == {"Groceries", "Dinner"}
    
    res_max = service.get_all_expenses(max_price=10)
    assert {e.title for e in res_max} == {"Coffee"}
    
    res_range = service.get_all_expenses(min_price=10, max_price=40)
    assert {e.title for e in res_range} == {"Movie", "Dinner"}
    
    # Category filter
    res_cat = service.get_all_expenses(category="Food")
    assert {e.title for e in res_cat} == {"Coffee", "Groceries", "Dinner"}
    
    # Date filter
    res_date_from = service.get_all_expenses(date_from=base_time - timedelta(days=2.5))
    assert {e.title for e in res_date_from} == {"Groceries", "Dinner"}
    
    res_date_to = service.get_all_expenses(date_to=base_time - timedelta(days=4))
    assert {e.title for e in res_date_to} == {"Coffee"}
    
    # Combined filter
    res_combo = service.get_all_expenses(category="Food", min_price=40)
    assert {e.title for e in res_combo} == {"Groceries"}

def test_get_user_expenses_filtered(populated_service):
    service, base_time = populated_service

    # Get all for user 1 (should be Coffee, Movie, Dinner)
    all_user1 = service.get_user_expenses(user_id=1)
    assert len(all_user1) == 3

    # Filter user 1 by category
    res_cat = service.get_user_expenses(user_id=1, category="Food")
    assert {e.title for e in res_cat} == {"Coffee", "Dinner"}
    
    # Filter user 1 by price
    res_price = service.get_user_expenses(user_id=1, min_price=20)
    assert {e.title for e in res_price} == {"Dinner"}
    
    # Filter user 1 by date
    res_date = service.get_user_expenses(user_id=1, date_from=base_time - timedelta(days=4))
    assert {e.title for e in res_date} == {"Movie", "Dinner"}

    # Filter user 1 but get no results (should be none)
    res_none = service.get_user_expenses(user_id=1, category="Entertainment", max_price=10)
    assert len(res_none) == 0

    # Check user 2 (should only be Groceries)
    res_user2 = service.get_user_expenses(user_id=2, category="Food")
    assert {e.title for e in res_user2} == {"Groceries"}