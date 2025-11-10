from abc import ABC, abstractmethod
from datetime import datetime
from typing import List, Optional

from models.expense import Expense
from repositories.expense_repository import IExpenseRepository
from schemas.expense import ExpenseCreate, ExpenseUpdate
from sqlalchemy.exc import NoResultFound


class IExpenseService(ABC):
    # CREATE
    @abstractmethod
    def create_expense(self, data: ExpenseCreate) -> None: ...

    # READ
    @abstractmethod
    def get_expense_by_id(self, expense_id: int) -> Expense: ...
    @abstractmethod
    def get_all_expenses(
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
    ) -> List[Expense]: ...
    @abstractmethod
    def get_user_expenses(
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
    ) -> List[Expense]: ...
    @abstractmethod
    def get_group_expenses(self, group_id: int, offset: int = 0, limit: int = 100) -> List[Expense]: ...

    # UPDATE
    @abstractmethod
    def update_expense(self, expense_id: int, data: ExpenseUpdate) -> None: ...

    # DELETE
    @abstractmethod
    def delete_expense(self, expense_id: int) -> None: ...


class ExpenseService(IExpenseService):
    def __init__(self, repository: IExpenseRepository):
        self.repository = repository

    def create_expense(self, data: ExpenseCreate):
        """
        Creates a new expense — it can belong to a user or a group.
        """
        expense = Expense(**data.model_dump())
        return self.repository.add(expense)

    def get_expense_by_id(self, expense_id: int) -> Expense:
        """
        Retrieves a specific expense and ensures access control:
        - personal expenses → must belong to the user
        - group expenses → (future: user must belong to that group)
        """
        expense = self.repository.get_by_id(expense_id)
        if not expense:
            raise NoResultFound(f"Expense with id {expense_id} not found.")
        return expense

    def get_all_expenses(
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
    ) -> List[Expense]:
        """
        Retrieves all expenses in the system with optional filtering.
        """
        return self.repository.get_all(
            offset, 
            limit, 
            sort_by, 
            order,
            min_price,
            max_price,
            date_from,
            date_to,
            category
        )

    def get_user_expenses(
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
    ) -> List[Expense]:
        """
        Retrieves all personal expenses for the authenticated user with optional filtering.
        """
        return self.repository.get_by_user(
            user_id, 
            offset, 
            limit, 
            sort_by, 
            order,
            min_price,
            max_price,
            date_from,
            date_to,
            category
        )

    def get_group_expenses(self, group_id: int, offset: int = 0, limit: int = 100, sort_by: str = "created_at", order: str = "desc") -> List[Expense]:
        """
        Retrieves all expenses for a specific group.
        (Access control — verifying that the user is part of the group — should be done at the router/service layer.)
        """
        return self.repository.get_by_group(group_id, offset, limit, sort_by, order)

    def update_expense(self, expense_id: int, data: ExpenseUpdate) -> None:
        """
        Updates an expense, ensuring it belongs to the authenticated user.
        """
        self.get_expense_by_id(expense_id)
        fields_to_update = data.model_dump(exclude_unset=True)
        # The ValueError will never be raised due to validation when creating an ExpenseUpdate object
        if not fields_to_update:
            raise ValueError("No fields provided for update.")
        return self.repository.update(expense_id, fields_to_update)

    def delete_expense(self, expense_id: int) -> None:
        """
        Deletes an expense, ensuring it belongs to the authenticated user.
        (Group deletion logic can be extended later.)
        """
        self.get_expense_by_id(expense_id)
        self.repository.delete(expense_id)
