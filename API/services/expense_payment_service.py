from abc import ABC, abstractmethod
from typing import List

from fastapi import HTTPException
from sqlalchemy.orm import Session

from models.expense import Expense
from models.expense_payment import ExpensePayment
from repositories.expense_payment_repository import IExpensePaymentRepository


class IExpensePaymentService(ABC):
    @abstractmethod
    def mark_paid(self, expense_id: int, payer_id: int, requester_id: int) -> ExpensePayment: ...
    
    @abstractmethod
    def unmark_paid(self, expense_id: int, payer_id: int, requester_id: int) -> None: ...
    
    @abstractmethod
    def get_payments(self, expense_id: int) -> List[ExpensePayment]: ...


class ExpensePaymentService(IExpensePaymentService):

    def __init__(self, repo: IExpensePaymentRepository, db: Session):
        self.repo = repo
        self.db = db

    def _validate_permissions(self, expense_id: int, requester_id: int) -> Expense:
        expense = (
            self.db.query(Expense)
            .filter_by(id=expense_id)
            .first()
        )
        if not expense:
            raise HTTPException(status_code=404, detail="Expense not found.")
        if expense.user_id != requester_id:
            raise HTTPException(status_code=403, detail="Not allowed.")
        return expense

    def mark_paid(self, expense_id: int, payer_id: int, requester_id: int) -> ExpensePayment:
        expense = self._validate_permissions(expense_id, requester_id)

        # Ensure payer is in the same group
        if expense.group_id:
            group_user_ids = {u.id for u in expense.group.users}
            if payer_id not in group_user_ids:
                raise HTTPException(status_code=400, detail="User not in group.")

        return self.repo.add(expense_id, payer_id)

    def unmark_paid(self, expense_id: int, payer_id: int, requester_id: int):
        self._validate_permissions(expense_id, requester_id)
        self.repo.remove(expense_id, payer_id)

    def get_payments(self, expense_id: int) -> List[ExpensePayment]:
        return self.repo.get_all_by_expense(expense_id)
