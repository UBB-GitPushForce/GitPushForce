from fastapi import APIRouter, Depends, HTTPException, Request
from sqlalchemy.orm import Session

from database import get_db
from services.expense_payment_service import ExpensePaymentService
from services.user_service import UserService
from schemas.expense_payment import ExpensePaymentResponse

router = APIRouter(tags=["Expense Payments"])


def get_current_user_id(request: Request, db: Session = Depends(get_db)) -> int:
    return UserService(db).auth_wrapper(request)


@router.post("/{expense_id}/pay/{payer_id}", response_model=ExpensePaymentResponse)
def mark_paid(
    expense_id: int,
    payer_id: int,
    requester_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    service = ExpensePaymentService(db)
    return service.mark_paid(expense_id, payer_id, requester_id)


@router.delete("/{expense_id}/pay/{payer_id}")
def unmark_paid(
    expense_id: int,
    payer_id: int,
    requester_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    service = ExpensePaymentService(db)
    service.unmark_paid(expense_id, payer_id, requester_id)
    return {"message": "Payment unmarked."}


@router.get("/{expense_id}/payments", response_model=list[ExpensePaymentResponse])
def get_payments(
    expense_id: int,
    requester_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    service = ExpensePaymentService(db)
    return service.get_payments(expense_id)
