import pytest
from models.expense import Expense
from schemas.expense import ExpenseCreate, ExpenseUpdate
from services.expense_service import ExpenseService
from sqlalchemy.exc import NoResultFound


class MockExpenseRepository:
    def __init__(self):
        self.expenses = {}
        self.next_id = 1

    def add(self, expense: Expense) -> Expense:
        expense.id = self.next_id
        self.next_id += 1
        self.expenses[expense.id] = expense
        return expense

    def get_by_id(self, expense_id: int):
        return self.expenses.get(expense_id)

    def get_all(self, offset: int = 0, limit: int = 100):
        items = list(self.expenses.values())
        return items[offset : offset + limit]

    def get_by_user(self, user_id: int, offset: int = 0, limit: int = 100):
        items = [e for e in self.expenses.values() if e.user_id == user_id]
        # mimic ordering by created_at desc is not critical for testing semantics
        return items[offset : offset + limit]

    def update(self, expense_id: int, fields: dict) -> Expense:
        exp = self.get_by_id(expense_id)
        if not exp:
            raise NoResultFound(f"Expense with id {expense_id} not found.")
        for k, v in fields.items():
            setattr(exp, k, v)
        return exp

    def delete(self, expense_id: int) -> None:
        exp = self.get_by_id(expense_id)
        if not exp:
            raise NoResultFound(f"Expense with id {expense_id} not found.")
        del self.expenses[expense_id]


@pytest.fixture
def expense_service():
    repo = MockExpenseRepository()
    service = ExpenseService(repo)
    return service


def _make_expense(service: ExpenseService, user_id: int = 1) -> Expense:
    create_in = ExpenseCreate(title="Coffee", category="Food", amount=3.5)
    return service.create_expense(create_in, user_id)


def test_create_expense(expense_service: ExpenseService):
    exp = _make_expense(expense_service, user_id=1)
    assert exp.id == 1
    assert exp.user_id == 1
    assert exp.title == "Coffee"


def test_get_expense_success(expense_service: ExpenseService):
    created = _make_expense(expense_service, user_id=2)
    fetched = expense_service.get_expense(created.id, user_id=2)
    assert fetched.id == created.id
    assert fetched.user_id == 2


def test_get_expense_not_found_or_forbidden(expense_service: ExpenseService):
    created = _make_expense(expense_service, user_id=1)
    with pytest.raises(NoResultFound):
        expense_service.get_expense(created.id, user_id=999)  # other user
    with pytest.raises(NoResultFound):
        expense_service.get_expense(999, user_id=1)  # nonexistent


def test_list_user_expenses(expense_service: ExpenseService):
    a = _make_expense(expense_service, user_id=1)
    b = _make_expense(expense_service, user_id=1)
    _ = _make_expense(expense_service, user_id=2)
    items = expense_service.get_user_expenses(user_id=1)
    ids = {e.id for e in items}
    assert {a.id, b.id}.issubset(ids)
    assert all(e.user_id == 1 for e in items)


def test_update_expense_success(expense_service: ExpenseService):
    created = _make_expense(expense_service, user_id=5)
    updated = expense_service.update_expense(
        created.id,
        ExpenseUpdate(title="Latte", amount=4.2),
        user_id=5,
    )
    assert updated.title == "Latte"
    assert updated.amount == 4.2


def test_update_expense_rejects_empty_payload(expense_service: ExpenseService):
    created = _make_expense(expense_service, user_id=3)
    with pytest.raises(ValueError):
        expense_service.update_expense(created.id, ExpenseUpdate(), user_id=3)


def test_update_expense_forbidden(expense_service: ExpenseService):
    created = _make_expense(expense_service, user_id=7)
    with pytest.raises(NoResultFound):
        expense_service.update_expense(created.id, ExpenseUpdate(title="X"), user_id=8)


def test_delete_expense_success(expense_service: ExpenseService):
    created = _make_expense(expense_service, user_id=4)
    # should not raise
    expense_service.delete_expense(created.id, user_id=4)
    with pytest.raises(NoResultFound):
        expense_service.get_expense(created.id, user_id=4)


def test_delete_expense_forbidden(expense_service: ExpenseService):
    created = _make_expense(expense_service, user_id=11)
    with pytest.raises(NoResultFound):
        expense_service.delete_expense(created.id, user_id=12)


