from abc import ABC, abstractmethod
from typing import List

from models.expense import Expense
from repositories.expense_repository import IExpenseRepository
from schemas.expense import ExpenseCreate, ExpenseUpdate
from sqlalchemy.exc import NoResultFound
from utils.helpers.logger import Logger


class IExpenseService(ABC):
    # CREATE
    @abstractmethod
    def create_expense(self, data: ExpenseCreate) -> None: ...

    # READ
    @abstractmethod
    def get_expense_by_id(self, expense_id: int) -> Expense: ...
    @abstractmethod
    def get_all_expenses(self, offset: int = 0, limit: int = 100) -> List[Expense]: ...
    @abstractmethod
    def get_user_expenses(self, user_id: int, offset: int = 0, limit: int = 100) -> List[Expense]: ...
    @abstractmethod
    def get_group_expenses(self, group_id: int, offset: int = 0, limit: int = 100) -> List[Expense]: ...

    # UPDATE
    @abstractmethod
    def update_expense(self, expense_id: int, data: ExpenseUpdate) -> None: ...

    # DELETE
    @abstractmethod
    def delete_expense(self, expense_id: int) -> None: ...


class ExpenseService(IExpenseService):
    logger = Logger()

    def __init__(self, repository: IExpenseRepository):
        self.repository = repository

    def create_expense(self, data: ExpenseCreate) -> None:
        self.logger.debug(f"Creating expense with data: {data}")
        expense = Expense(**data.model_dump())
        self.repository.add(expense)

    def get_expense_by_id(self, expense_id: int) -> Expense:
        self.logger.debug(f"Retrieving expense with id {expense_id}")
        expense = self.repository.get_by_id(expense_id)
        if not expense:
            self.logger.warning(f"Expense with id {expense_id} not found.")
            raise NoResultFound(f"Expense with id {expense_id} not found.")
        return expense

    def get_all_expenses(self, offset: int = 0, limit: int = 100) -> List[Expense]:
        self.logger.debug(f"Retrieving all expenses with offset {offset}, limit {limit}")
        return self.repository.get_all(offset, limit)

    def get_user_expenses(self, user_id: int, offset: int = 0, limit: int = 100) -> List[Expense]:
        self.logger.debug(f"Retrieving expenses for user_id {user_id} with offset {offset}, limit {limit}")
        return self.repository.get_by_user(user_id, offset=offset, limit=limit)

    def get_group_expenses(self, group_id: int, offset: int = 0, limit: int = 100) -> List[Expense]:
        self.logger.debug(f"Retrieving expenses for group_id {group_id} with offset {offset}, limit {limit}")
        return self.repository.get_by_group(group_id, offset=offset, limit=limit)

    def update_expense(self, expense_id: int, data: ExpenseUpdate) -> None:
        self.logger.debug(f"Updating expense with id {expense_id}")
        self.get_expense_by_id(expense_id)
        fields_to_update = data.model_dump(exclude_unset=True)
        if not fields_to_update:
            self.logger.warning(f"No fields provided for update for expense with id {expense_id}")
            raise ValueError("No fields provided for update.")
        return self.repository.update(expense_id, fields_to_update)

    def delete_expense(self, expense_id: int) -> None:
        self.logger.debug(f"Deleting expense with id {expense_id}")
        self.get_expense_by_id(expense_id)
        self.repository.delete(expense_id)
