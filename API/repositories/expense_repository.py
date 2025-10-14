from abc import ABC, abstractmethod
from typing import Optional, List

from sqlalchemy import select
from sqlalchemy.exc import NoResultFound
from sqlalchemy.orm import Session

from models.expense import Expense


class IExpenseRepository(ABC):
    @abstractmethod
    def add(self, expense: Expense) -> Expense: ...
    @abstractmethod
    def get_by_id(self, expense_id: int) -> Optional[Expense]: ...
    @abstractmethod
    def get_all(self, offset: int = 0, limit: int = 100) -> List[Expense]: ...
    @abstractmethod
    def get_by_user(self, user_id: int, offset: int = 0, limit: int = 100) -> List[Expense]: ...
    @abstractmethod
    def update(self, expense_id: int, fields: dict) -> Expense: ...
    @abstractmethod
    def delete(self, expense_id: int) -> None: ...


class ExpenseRepository(IExpenseRepository):
    def __init__(self, db: Session):
        self.db = db

    def add(self, expense: Expense) -> Expense:
        self.db.add(expense)
        self.db.commit()
        self.db.refresh(expense)
        return expense

    def get_by_id(self, expense_id: int) -> Optional[Expense]:
        stmt = select(Expense).where(Expense.id == expense_id)
        return self.db.scalars(stmt).first()

    def get_all(self, offset: int = 0, limit: int = 100) -> List[Expense]:
        stmt = (
            select(Expense)
            .order_by(Expense.id)
            .offset(offset)
            .limit(limit)
        )
        return list(self.db.scalars(stmt))

    def get_by_user(self, user_id: int, offset: int = 0, limit: int = 100) -> List[Expense]:
        stmt = (
            select(Expense)
            .where(Expense.user_id == user_id)
            .order_by(Expense.created_at.desc())
            .offset(offset)
            .limit(limit)
        )
        return list(self.db.scalars(stmt))

    def update(self, expense_id: int, fields: dict) -> Expense:
        expense = self.get_by_id(expense_id)
        if not expense:
            raise NoResultFound(f"Expense with id {expense_id} not found.")

        for key, value in fields.items():
            if hasattr(expense, key):
                setattr(expense, key, value)

        self.db.commit()
        self.db.refresh(expense)
        return expense

    def delete(self, expense_id: int) -> None:
        expense = self.get_by_id(expense_id)
        if not expense:
            raise NoResultFound(f"Expense with id {expense_id} not found.")

        self.db.delete(expense)
        self.db.commit()
