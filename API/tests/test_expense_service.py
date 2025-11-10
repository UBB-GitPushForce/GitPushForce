from datetime import datetime

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
        expense.created_at = datetime.utcnow()
        self.expenses[self.counter] = expense
        self.counter += 1

    def get_by_id(self, expense_id: int):
        return self.expenses.get(expense_id)

    def get_all(self, offset: int, limit: int):
        return list(self.expenses.values())[offset: offset + limit]

    def get_by_user(self, user_id: int, offset: int, limit: int):
        return [e for e in self.expenses.values() if e.user_id == user_id][offset: offset + limit]

    def get_by_group(self, group_id: int, offset: int, limit: int):
        return [e for e in self.expenses.values() if e.group_id == group_id][offset: offset + limit]

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
    expense_service.create_expense(data)
    assert len(mock_repo.expenses) == 1
    exp = list(mock_repo.expenses.values())[0]
    assert exp.title == "Lunch"
    assert exp.amount == 12.5
    assert exp.user_id == 1
    assert exp.group_id is None


def test_get_expense_by_id_success(expense_service, mock_repo, sample_expense_data):
    data = ExpenseCreate(**sample_expense_data)
    expense_service.create_expense(data)
    expense = expense_service.get_expense_by_id(1)
    assert isinstance(expense, Expense)
    assert expense.id == 1


def test_get_expense_by_id_not_found(expense_service):
    with pytest.raises(NoResultFound):
        expense_service.get_expense_by_id(999)


def test_get_all_expenses(expense_service, mock_repo, sample_expense_data):
    for i in range(3):
        data = ExpenseCreate(**sample_expense_data)
        expense_service.create_expense(data)
    result = expense_service.get_all_expenses()
    assert len(result) == 3


def test_get_user_expenses(expense_service, mock_repo):
    e1 = Expense(title="A", category="Food", amount=10, user_id=1)
    e2 = Expense(title="B", category="Food", amount=15, user_id=2)
    mock_repo.add(e1)
    mock_repo.add(e2)
    user_expenses = expense_service.get_user_expenses(user_id=1)
    assert len(user_expenses) == 1
    assert user_expenses[0].user_id == 1


def test_get_group_expenses(expense_service, mock_repo):
    e1 = Expense(title="G1", category="Trip", amount=50, group_id=10)
    e2 = Expense(title="G2", category="Trip", amount=30, group_id=20)
    mock_repo.add(e1)
    mock_repo.add(e2)
    group_expenses = expense_service.get_group_expenses(group_id=10)
    assert len(group_expenses) == 1
    assert group_expenses[0].group_id == 10


def test_update_expense_success(expense_service, mock_repo, sample_expense_data):
    data = ExpenseCreate(**sample_expense_data)
    expense_service.create_expense(data)
    update_data = ExpenseUpdate(title="Updated Lunch", user_id=1)
    updated = expense_service.update_expense(1, update_data)
    assert updated.title == "Updated Lunch"
    assert mock_repo.expenses[1].title == "Updated Lunch"


def test_delete_expense_success(expense_service, mock_repo, sample_expense_data):
    data = ExpenseCreate(**sample_expense_data)
    expense_service.create_expense(data)
    assert len(mock_repo.expenses) == 1
    expense_service.delete_expense(1)
    assert len(mock_repo.expenses) == 0


def test_delete_expense_not_found(expense_service):
    with pytest.raises(NoResultFound):
        expense_service.delete_expense(123)
