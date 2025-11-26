from abc import ABC, abstractmethod
from typing import List

from fastapi import HTTPException
from models.expense import Expense
from repositories.expense_repository import IExpenseRepository
from schemas.expense import ExpenseCreate, ExpenseUpdate
from sqlalchemy.exc import NoResultFound
from sqlalchemy.orm import Session


class IExpenseService(ABC):
    @abstractmethod
    def create_expense(self, data: ExpenseCreate, user_id: int) -> Expense: ...
    @abstractmethod
    def get_expense_by_id(self, expense_id: int) -> Expense: ...
    @abstractmethod
    def get_all_expenses(self, *args, **kwargs) -> List[Expense]: ...
    @abstractmethod
    def get_user_expenses(self, *args, **kwargs) -> List[Expense]: ...
    @abstractmethod
    def get_group_expenses(self, *args, **kwargs) -> List[Expense]: ...
    @abstractmethod
    def update_expense(self, expense_id: int, data: ExpenseUpdate, requester_id: int) -> None: ...
    @abstractmethod
    def delete_expense(self, expense_id: int, requester_id: int) -> None: ...


class ExpenseService(IExpenseService):
    def __init__(self, repository: IExpenseRepository, db: Session):
        self.repository = repository
        self.db = db

    def _validate_owner(self, expense_id: int, requester_id: int) -> Expense:
        expense = self.repository.get_by_id(expense_id)
        if not expense:
            raise NoResultFound("Expense not found.")
        if expense.user_id != requester_id:
            raise HTTPException(status_code=403, detail="Not allowed to modify this expense.")
        return expense

    def create_expense(self, data: ExpenseCreate, user_id: int) -> Expense:
        expense = Expense(
            **data.model_dump(),
            user_id=user_id
        )
        return self.repository.add(expense)

    def get_expense_by_id(self, expense_id: int) -> Expense:
        expense = self.repository.get_by_id(expense_id)
        if not expense:
            raise NoResultFound("Expense not found.")
        return expense

    def get_all_expenses(self, *args, **kwargs):
        return self.repository.get_all(*args, **kwargs)

    def get_user_expenses(self, *args, **kwargs):
        return self.repository.get_by_user(*args, **kwargs)

    def get_group_expenses(self, *args, **kwargs):
        return self.repository.get_by_group(*args, **kwargs)

    def update_expense(self, expense_id: int, data: ExpenseUpdate, requester_id: int) -> None:
        self._validate_owner(expense_id, requester_id)
        fields = data.model_dump(exclude_unset=True)
        fields.pop("user_id", None)
        self.repository.update(expense_id, fields)

    def delete_expense(self, expense_id: int, requester_id: int) -> None:
        self._validate_owner(expense_id, requester_id)
        self.repository.delete(expense_id)
