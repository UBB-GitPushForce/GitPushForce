from dependencies.di import get_expense_payment_service
from fastapi import APIRouter, Depends
from services.expense_payment_service import IExpensePaymentService
from utils.helpers.jwt_utils import JwtUtils

router = APIRouter(tags=["Expense Payments"])


@router.post("/{expense_id}/pay/{payer_id}")
def mark_paid(
    expense_id: int,
    payer_id: int,
    requester_id: int = Depends(JwtUtils.auth_wrapper),
    expense_payment_service: IExpensePaymentService = Depends(get_expense_payment_service)
):
    return expense_payment_service.mark_paid(expense_id, payer_id, requester_id)


@router.delete("/{expense_id}/pay/{payer_id}")
def unmark_paid(
    expense_id: int,
    payer_id: int,
    requester_id: int = Depends(JwtUtils.auth_wrapper),
    expense_payment_service: IExpensePaymentService = Depends(get_expense_payment_service)
):
    return expense_payment_service.unmark_paid(expense_id, payer_id, requester_id)


@router.get("/{expense_id}/payments")
def get_payments(
    expense_id: int,
    requester_id: int = Depends(JwtUtils.auth_wrapper),
    expense_payment_service: IExpensePaymentService = Depends(get_expense_payment_service)
):
    return expense_payment_service.get_payments(expense_id, requester_id)
