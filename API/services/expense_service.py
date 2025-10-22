from abc import ABC, abstractmethod
from typing import List

from sqlalchemy.exc import NoResultFound

from models.expense import Expense
from schemas.expense import ExpenseCreate, ExpenseUpdate
from repositories.expense_repository import IExpenseRepository


class IExpenseService(ABC):
    @abstractmethod
    def create_expense(self, expense_in: ExpenseCreate, user_id: int) -> Expense: ...
    @abstractmethod
    def get_expense(self, expense_id: int, user_id: int) -> Expense: ...
    @abstractmethod
    def get_user_expenses(self, user_id: int, offset: int = 0, limit: int = 100) -> List[Expense]: ...
    @abstractmethod
    def update_expense(self, expense_id: int, expense_in: ExpenseUpdate, user_id: int) -> Expense: ...
    @abstractmethod
    def delete_expense(self, expense_id: int, user_id: int) -> None: ...


class ExpenseService:
    def __init__(self, repository: IExpenseRepository):
        self.repository = repository

    def create_expense(self, expense_in: ExpenseCreate, user_id: int) -> Expense:
        """
        Creates a new expense from a validated Pydantic model.
        """
        expense = Expense(
            user_id=user_id,
            title=expense_in.title,
            category=expense_in.category,
            amount=expense_in.amount,
        )
        return self.repository.add(expense)

    def get_expense(self, expense_id: int, user_id: int) -> Expense:
        """
        Retrieves a specific expense, ensuring it belongs to the authenticated user.
        Raises NoResultFound if the expense does not exist or does not belong to the user.
        """
        expense = self.repository.get_by_id(expense_id)

        if not expense or expense.user_id != user_id:
            # Raise an error if not found OR if it belongs to another user
            raise NoResultFound(f"Expense with id {expense_id} not found or access denied.")
        
        return expense

    def get_user_expenses(self, user_id: int, offset: int = 0, limit: int = 100) -> List[Expense]:
        """
        Retrieves all expenses for the authenticated user with pagination.
        """
        return self.repository.get_by_user(user_id, offset=offset, limit=limit)
    
    def update_expense(self, expense_id: int, expense_in: ExpenseUpdate, user_id: int) -> Expense:
        """
        Updates an expense, ensuring it belongs to the authenticated user.
        Raises NoResultFound if the expense does not exist or does not belong to the user.
        """
        # Check if the expense exists and belongs to the user
        self.get_expense(expense_id, user_id)

        # Create a dictionary of fields to update, excluding None values
        fields_to_update = expense_in.model_dump(exclude_unset=True)

        if not fields_to_update:
            raise ValueError("No fields provided for update.")
        
        return self.repository.update(expense_id, fields_to_update)
    
    def delete_expense(self, expense_id: int, user_id: int) -> None:
        """
        Deletes an expense, ensuring it belongs to the authenticated user.
        Raises NoResultFound if the expense does not exist or does not belong to the user.
        """
        # Check if the expense exists and belongs to the user
        self.get_expense(expense_id, user_id)

        self.repository.delete(expense_id)
