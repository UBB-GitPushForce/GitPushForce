from sqlalchemy.orm import Session
from models.expense_payment import ExpensePayment
from typing import List, Optional


class ExpensePaymentRepository:
    def __init__(self, db: Session):
        self.db = db

    def add(self, expense_id: int, user_id: int) -> ExpensePayment:
        payment = ExpensePayment(expense_id=expense_id, user_id=user_id)
        self.db.add(payment)
        self.db.commit()
        self.db.refresh(payment)
        return payment

    def remove(self, expense_id: int, user_id: int) -> None:
        payment = self.db.query(ExpensePayment).filter_by(
            expense_id=expense_id, user_id=user_id
        ).first()

        if payment:
            self.db.delete(payment)
            self.db.commit()

    def get_all_by_expense(self, expense_id: int) -> List[ExpensePayment]:
        return list(self.db.query(ExpensePayment).filter_by(expense_id=expense_id))
