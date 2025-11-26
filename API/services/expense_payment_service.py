from fastapi import HTTPException
from models.expense import Expense
from repositories.expense_payment_repository import ExpensePaymentRepository
from sqlalchemy.orm import Session


class ExpensePaymentService:

    def __init__(self, db: Session):
        self.db = db
        self.repo = ExpensePaymentRepository(db)

    def _validate_permissions(self, expense_id: int, requester_id: int):
        expense = self.db.query(Expense).filter_by(id=expense_id).first()
        if not expense:
            raise HTTPException(status_code=404, detail="Expense not found.")

        if expense.user_id != requester_id:
            raise HTTPException(status_code=403, detail="You are not allowed to mark payments on this expense.")

        return expense

    def mark_paid(self, expense_id: int, payer_id: int, requester_id: int):
        expense = self._validate_permissions(expense_id, requester_id)

        # Check payer is in the same group
        group_user_ids = {u.id for u in expense.group.users}
        if payer_id not in group_user_ids:
            raise HTTPException(status_code=400, detail="User is not part of this group.")

        return self.repo.add(expense_id, payer_id)

    def unmark_paid(self, expense_id: int, payer_id: int, requester_id: int):
        self._validate_permissions(expense_id, requester_id)
        self.repo.remove(expense_id, payer_id)

    def get_payments(self, expense_id: int):
        return self.repo.get_all_by_expense(expense_id)
