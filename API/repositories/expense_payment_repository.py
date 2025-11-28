from abc import ABC, abstractmethod
from typing import List, Optional

from models.expense_payment import ExpensePayment
from sqlalchemy import select
from sqlalchemy.orm import Session


class IExpensePaymentRepository(ABC):
    """
    Interface for the expense payment repository. Ensures loose coupling.
    """

    @abstractmethod
    def add(self, expense_id: int, user_id: int) -> ExpensePayment: ...
    
    @abstractmethod
    def remove(self, expense_id: int, user_id: int) -> None: ...
    
    @abstractmethod
    def get_all_by_expense(self, expense_id: int) -> List[ExpensePayment]: ...

    @abstractmethod
    def get_payment(self, expense_id: int, user_id: int) -> Optional[ExpensePayment]: ...


class ExpensePaymentRepository(IExpensePaymentRepository):
    """
    Implementation of IExpensePaymentRepository.
    """

    def __init__(self, db: Session):
        self.db = db

    def add(self, expense_id: int, user_id: int) -> ExpensePayment:
        """
        Adds a payment marking that a user has paid for an expense.
        """
        payment = ExpensePayment(expense_id=expense_id, user_id=user_id)
        self.db.add(payment)
        self.db.commit()
        self.db.refresh(payment)
        return payment

    def remove(self, expense_id: int, user_id: int) -> None:
        """
        Removes a payment record for a specific expense and user.
        """
        payment = self.get_payment(expense_id, user_id)
        if payment:
            self.db.delete(payment)
            self.db.commit()

    def get_payment(self, expense_id: int, user_id: int) -> Optional[ExpensePayment]:
        """
        Retrieves a single payment record for a user-expense pair.
        """
        stmt = select(ExpensePayment).where(
            ExpensePayment.expense_id == expense_id,
            ExpensePayment.user_id == user_id
        )
        return self.db.scalars(stmt).first()

    def get_all_by_expense(self, expense_id: int) -> List[ExpensePayment]:
        """
        Retrieves all payment records for a given expense.
        """
        stmt = select(ExpensePayment).where(
            ExpensePayment.expense_id == expense_id
        )
        return list(self.db.scalars(stmt))
