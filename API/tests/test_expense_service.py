from datetime import datetime, timedelta
from operator import attrgetter

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
        expense.created_at = datetime.utcnow() + timedelta(seconds=self.counter)  # ensure unique timestamps
        self.expenses[self.counter] = expense
        self.counter += 1
        return expense

    def get_by_id(self, expense_id: int):
        return self.expenses.get(expense_id)

    def get_all(self, offset: int = 0, limit: int = 100, sort_by: str = "created_at", order: str = "desc"):
        expenses = list(self.expenses.values())
        expenses.sort(key=attrgetter(sort_by), reverse=(order.lower() == "desc"))
        return expenses[offset: offset + limit]

    def get_by_user(self, user_id: int, offset: int = 0, limit: int = 100, sort_by: str = "created_at", order: str = "desc"):
        expenses = [e for e in self.expenses.values() if e.user_id == user_id]
        expenses.sort(key=attrgetter(sort_by), reverse=(order.lower() == "desc"))
        return expenses[offset: offset + limit]

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
