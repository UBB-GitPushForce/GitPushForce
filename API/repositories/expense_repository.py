from abc import ABC, abstractmethod
from typing import List, Optional

from models.expense import Expense
from sqlalchemy import select
from sqlalchemy.exc import NoResultFound, IntegrityError, SQLAlchemyError
from sqlalchemy.orm import Session


class IExpenseRepository(ABC):
    # CREATE
    @abstractmethod
    def add(self, expense: Expense) -> None: ...

    # READ
    @abstractmethod
    def get_by_id(self, expense_id: int) -> Optional[Expense]: ...
    @abstractmethod
    def get_all(self, offset: int, limit: int) -> List[Expense]: ...
    @abstractmethod
    def get_by_user(self, user_id: int, offset: int, limit: int) -> List[Expense]: ...
    @abstractmethod
    def get_by_group(self, group_id: int, offset: int, limit: int) -> List[Expense]: ...

    # UPDATE
    @abstractmethod
    def update(self, expense_id: int, fields: dict) -> None: ...

    # DELETE
    @abstractmethod
    def delete(self, expense_id: int) -> None: ...


class ExpenseRepository(IExpenseRepository):
    def __init__(self, db: Session):
        self.db = db

    def add(self, expense: Expense) -> None:
        self.db.add(expense)
        self.db.commit()
        self.db.refresh(expense)

    def get_by_id(self, expense_id: int) -> Optional[Expense]:
        stmt = select(Expense).where(Expense.id == expense_id)
        return self.db.scalars(stmt).first()

    def get_all(self, offset: int, limit: int) -> List[Expense]:
        stmt = (
            select(Expense)
            .order_by(Expense.created_at.desc())
            .offset(offset)
            .limit(limit)
        )
        return list(self.db.scalars(stmt))

    def get_by_user(self, user_id: int, offset: int, limit: int) -> List[Expense]:
        stmt = (
            select(Expense)
            .where(Expense.user_id == user_id)
            .order_by(Expense.created_at.desc())
            .offset(offset)
            .limit(limit)
        )
        return list(self.db.scalars(stmt))

    def get_by_group(self, group_id: int, offset: int, limit: int) -> List[Expense]:
        stmt = (
            select(Expense)
            .where(Expense.group_id == group_id)
            .order_by(Expense.created_at.desc())
            .offset(offset)
            .limit(limit)
        )
        return list(self.db.scalars(stmt))

    def update(self, expense_id: int, fields: dict) -> None:
        expense = self.get_by_id(expense_id)
        for key, value in fields.items():
            if hasattr(expense, key):
                setattr(expense, key, value)
        self.db.commit()
        self.db.refresh(expense)

    def delete(self, expense_id: int) -> None:
        expense = self.get_by_id(expense_id)
        self.db.delete(expense)
        self.db.commit()
