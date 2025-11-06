from abc import ABC, abstractmethod
from typing import List

from models.expense import Expense
from repositories.expense_repository import IExpenseRepository
from schemas.expense import ExpenseCreate, ExpenseUpdate
from sqlalchemy.exc import NoResultFound


class IExpenseService(ABC):
    @abstractmethod
    def create_expense(self, expense_in: ExpenseCreate, user_id: int) -> Expense: ...
    @abstractmethod
    def get_expense(self, expense_id: int, user_id: int) -> Expense: ...
    @abstractmethod
    def get_user_expenses(self, user_id: int, offset: int = 0, limit: int = 100) -> List[Expense]: ...
    @abstractmethod
    def get_group_expenses(self, group_id: int, offset: int = 0, limit: int = 100) -> List[Expense]: ...
    @abstractmethod
    def update_expense(self, expense_id: int, expense_in: ExpenseUpdate, user_id: int) -> Expense: ...
    @abstractmethod
    def delete_expense(self, expense_id: int, user_id: int) -> None: ...


class ExpenseService(IExpenseService):
    def __init__(self, repository: IExpenseRepository):
        self.repository = repository

    # -------------------------------
    # CREATE
    # -------------------------------
    def create_expense(self, expense_in: ExpenseCreate, user_id: int) -> Expense:
        """
        Creates a new expense — it can belong to a user or a group.
        The ExpenseCreate model ensures that exactly one of user_id/group_id is set.
        """
        expense_data = expense_in.model_dump()

        # Default behavior: if group_id is not provided, it's a personal expense
        if not expense_data.get("user_id") and not expense_data.get("group_id"):
            expense_data["user_id"] = user_id
        elif expense_data.get("group_id") and expense_data.get("user_id"):
            raise ValueError("Expense cannot have both user_id and group_id set.")

        expense = Expense(**expense_data)
        return self.repository.add(expense)

    # -------------------------------
    # READ
    # -------------------------------
    def get_expense(self, expense_id: int, user_id: int) -> Expense:
        """
        Retrieves a specific expense and ensures access control:
        - personal expenses → must belong to the user
        - group expenses → (future: user must belong to that group)
        """
        expense = self.repository.get_by_id(expense_id)
        if not expense:
            raise NoResultFound(f"Expense with id {expense_id} not found.")

        if expense.user_id and expense.user_id != user_id:
            raise NoResultFound(f"Expense with id {expense_id} not found or access denied.")

        return expense

    def get_user_expenses(self, user_id: int, offset: int = 0, limit: int = 100) -> List[Expense]:
        """
        Retrieves all personal expenses for the authenticated user.
        """
        return self.repository.get_by_user(user_id, offset=offset, limit=limit)

    def get_group_expenses(self, group_id: int, offset: int = 0, limit: int = 100) -> List[Expense]:
        """
        Retrieves all expenses for a specific group.
        (Access control — verifying that the user is part of the group — should be done at the router/service layer.)
        """
        return self.repository.get_by_group(group_id, offset=offset, limit=limit)

    # -------------------------------
    # UPDATE
    # -------------------------------
    def update_expense(self, expense_id: int, expense_in: ExpenseUpdate, user_id: int) -> Expense:
        """
        Updates an expense, ensuring it belongs to the authenticated user.
        """
        self.get_expense(expense_id, user_id)

        fields_to_update = expense_in.model_dump(exclude_unset=True)
        if not fields_to_update:
            raise ValueError("No fields provided for update.")

        return self.repository.update(expense_id, fields_to_update)

    # -------------------------------
    # DELETE
    # -------------------------------
    def delete_expense(self, expense_id: int, user_id: int) -> None:
        """
        Deletes an expense, ensuring it belongs to the authenticated user.
        (Group deletion logic can be extended later.)
        """
        self.get_expense(expense_id, user_id)
        self.repository.delete(expense_id)
