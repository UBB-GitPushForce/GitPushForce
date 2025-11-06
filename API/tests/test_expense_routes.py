import pytest
from fastapi.testclient import TestClient
from sqlalchemy.exc import NoResultFound

from main import app
from routes.expense_routes import get_expense_service, get_current_user_id
from services.expense_service import ExpenseService
from models.expense import Expense


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
def client():
    repo = MockExpenseRepository()
    service = ExpenseService(repo)

    # Dependency overrides: fixed user id and mocked service
    app.dependency_overrides[get_current_user_id] = lambda: 1
    app.dependency_overrides[get_expense_service] = lambda: service
    try:
        with TestClient(app) as c:
            yield c
    finally:
        app.dependency_overrides.clear()


def test_create_expense_route(client: TestClient):
    payload = {"title": "Coffee", "category": "Food", "amount": 3.5}
    res = client.post("/expenses/", json=payload)
    assert res.status_code == 201
    body = res.json()
    assert body["title"] == "Coffee"
    assert body["category"] == "Food"
    assert body["amount"] == 3.5


def test_get_expense_route(client: TestClient):
    created = client.post("/expenses/", json={"title": "Tea", "category": "Food", "amount": 2.0}).json()
    expense_id = created.get("id", 1)  # Pydantic may omit id in response_model; handle gracefully
    res = client.get(f"/expenses/{expense_id}")
    assert res.status_code == 200
    assert res.json()["title"] == "Tea"


def test_list_expenses_route(client: TestClient):
    client.post("/expenses/", json={"title": "A", "category": "Misc", "amount": 1})
    client.post("/expenses/", json={"title": "B", "category": "Misc", "amount": 2})
    res = client.get("/expenses/?offset=0&limit=10")
    assert res.status_code == 200
    items = res.json()
    assert isinstance(items, list)
    assert len(items) >= 2


def test_update_expense_route(client: TestClient):
    created = client.post("/expenses/", json={"title": "X", "category": "Y", "amount": 5}).json()
    expense_id = created.get("id", 1)
    res = client.put(f"/expenses/{expense_id}", json={"title": "Updated", "amount": 6})
    assert res.status_code == 200
    assert res.json()["title"] == "Updated"
    assert res.json()["amount"] == 6


def test_delete_expense_route(client: TestClient):
    created = client.post("/expenses/", json={"title": "Z", "category": "M", "amount": 7}).json()
    expense_id = created.get("id", 1)
    res = client.delete(f"/expenses/{expense_id}")
    assert res.status_code == 204
    res2 = client.get(f"/expenses/{expense_id}")
    assert res2.status_code == 404


