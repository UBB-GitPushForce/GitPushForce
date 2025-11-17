from abc import ABC, abstractmethod
from datetime import datetime
from typing import List, Optional

from models.expense import Expense
from repositories.expense_repository import IExpenseRepository
from schemas.expense import ExpenseCreate, ExpenseUpdate
from sqlalchemy.exc import NoResultFound


class IExpenseService(ABC):
    """
    Defines the interface for expense service operations.

    Args:
        data (ExpenseCreate) expense creation payload
        expense_id (int) id of the expense
        offset (int) items to skip
        limit (int) maximum number of items to return
        sort_by (str) field to sort by
        order (str) sorting order
        min_price (float) minimum amount filter
        max_price (float) maximum amount filter
        date_from (datetime) start date filter
        date_to (datetime) end date filter
        category (str) category filter
        user_id (int) id of the user
        group_ids (list[int]) group filter list
        group_id (int) id of the group
        data (ExpenseUpdate) update payload

    Returns:
        Expense or list[Expense] depending on the method

    Exceptions:
        NoResultFound raised when an expense is not found
        ValueError raised when update data is invalid
    """

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
        category: Optional[str] = None,
        group_ids: Optional[List[int]] = None
    ) -> List[Expense]: ...
    @abstractmethod
    def get_group_expenses(
        self, 
        group_id: int, 
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

    # UPDATE
    @abstractmethod
    def update_expense(self, expense_id: int, data: ExpenseUpdate) -> None: ...

    # DELETE
    @abstractmethod
    def delete_expense(self, expense_id: int) -> None: ...


class ExpenseService(IExpenseService):
    def __init__(self, repository: IExpenseRepository):
        """
        Initializes the expense service.

        Args:
            repository (IExpenseRepository) repository used for persistence

        Returns:
            None

        Exceptions:
            None
        """
        self.repository = repository

    def create_expense(self, data: ExpenseCreate):
        """
        Creates a new expense.

        Args:
            data (ExpenseCreate) validated creation data

        Returns:
            Expense created expense object

        Exceptions:
            None
        """
        expense = Expense(**data.model_dump())
        return self.repository.add(expense)

    def get_expense_by_id(self, expense_id: int) -> Expense:
        """
        Retrieves an expense by its id.

        Args:
            expense_id (int) id of the expense

        Returns:
            Expense matching expense

        Exceptions:
            NoResultFound raised when the expense does not exist
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
        Retrieves all expenses across the system with filtering.

        Args:
            offset (int) items to skip
            limit (int) maximum items to return
            sort_by (str) sorting field
            order (str) sorting direction
            min_price (float) minimum price filter
            max_price (float) maximum price filter
            date_from (datetime) filter by start date
            date_to (datetime) filter by end date
            category (str) category filter

        Returns:
            list[Expense] filtered list of expenses

        Exceptions:
            None
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
        category: Optional[str] = None,
        group_ids: Optional[List[int]] = None
    ) -> List[Expense]:
        """
        Retrieves expenses belonging to a specific user.

        Args:
            user_id (int) id of the user
            offset (int) items to skip
            limit (int) maximum items to return
            sort_by (str) sorting field
            order (str) sort direction
            min_price (float) minimum price filter
            max_price (float) maximum price filter
            date_from (datetime) start date filter
            date_to (datetime) end date filter
            category (str) category filter
            group_ids (list[int]) list of group ids to filter by

        Returns:
            list[Expense] filtered user expenses

        Exceptions:
            None
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
            category,
            group_ids
        )

    def get_group_expenses(
        self, 
        group_id: int, 
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
        Retrieves expenses belonging to a specific group.

        Args:
            group_id (int) id of the group
            offset (int) items to skip
            limit (int) maximum items to return
            sort_by (str) sort field
            order (str) sort direction
            min_price (float) minimum price filter
            max_price (float) maximum price filter
            date_from (datetime) filter by start date
            date_to (datetime) filter by end date
            category (str) category filter

        Returns:
            list[Expense] filtered group expenses

        Exceptions:
            None
        """
        return self.repository.get_by_group(
            group_id, 
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

    def update_expense(self, expense_id: int, data: ExpenseUpdate) -> None:
        """
        Updates an existing expense.

        Args:
            expense_id (int) id of the expense
            data (ExpenseUpdate) validated update fields

        Returns:
            None

        Exceptions:
            NoResultFound raised when expense is not found
            ValueError raised when no update fields are provided
        """
        self.get_expense_by_id(expense_id)
        fields_to_update = data.model_dump(exclude_unset=True)

        if not fields_to_update:
            raise ValueError("No fields provided for update.")

        return self.repository.update(expense_id, fields_to_update)

    def delete_expense(self, expense_id: int) -> None:
        """
        Deletes an expense.

        Args:
            expense_id (int) id of the expense

        Returns:
            None

        Exceptions:
            NoResultFound raised when expense does not exist
        """
        self.get_expense_by_id(expense_id)
        self.repository.delete(expense_id)
