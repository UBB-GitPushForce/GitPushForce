from abc import ABC, abstractmethod

from fastapi import HTTPException
from models.expense import Expense
from repositories.expense_payment_repository import IExpensePaymentRepository
from repositories.expense_repository import IExpenseRepository
from repositories.group_repository import IGroupRepository
from repositories.user_repository import IUserRepository
from schemas.api_response import APIResponse
from schemas.expense_payment import ExpensePaymentResponse
from utils.helpers.constants import (
    STATUS_BAD_REQUEST,
    STATUS_FORBIDDEN,
    STATUS_NOT_FOUND,
)


class IExpensePaymentService(ABC):
    """
    Interface for expense payment operations.
    """

    @abstractmethod
    def mark_paid(self, expense_id: int, payer_id: int, requester_id: int) -> APIResponse: ...

    @abstractmethod
    def unmark_paid(self, expense_id: int, payer_id: int, requester_id: int) -> APIResponse: ...

    @abstractmethod
    def get_payments(self, expense_id: int, requester_id: int) -> APIResponse: ...


class ExpensePaymentService(IExpensePaymentService):
    """
    Manages payment statuses for expenses.
    """

    def __init__(
        self,
        repo: IExpensePaymentRepository,
        expense_repo: IExpenseRepository,
        group_repo: IGroupRepository,
        user_repo: IUserRepository,
    ):
        self.repo = repo
        self.expense_repo = expense_repo
        self.group_repo = group_repo
        self.user_repo = user_repo

    def _validate_permissions(self, expense_id: int, requester_id: int) -> Expense:
        """
        Verifies requester is the owner of the expense.
        """
        expense = self.expense_repo.get_by_id(expense_id)
        if not expense:
            raise HTTPException(status_code=STATUS_NOT_FOUND, detail="Expense not found.")
        if expense.user_id != requester_id:
            raise HTTPException(status_code=STATUS_FORBIDDEN, detail="Not allowed.")
        return expense

    def mark_paid(self, expense_id: int, payer_id: int, requester_id: int) -> APIResponse:
        """
        Marks the expense as paid by a specific user.
        """
        expense = self._validate_permissions(expense_id, requester_id)

        payer = self.user_repo.get_by_id(payer_id)
        if not payer:
            raise HTTPException(status_code=STATUS_NOT_FOUND, detail="User not found.")

        if expense.group_id:
            group = self.group_repo.get_by_id(expense.group_id)
            if payer_id not in {u.id for u in group.users}:
                raise HTTPException(status_code=STATUS_BAD_REQUEST, detail="User not in group.")

        payment = self.repo.add(expense_id, payer_id)
        response = ExpensePaymentResponse.model_validate(payment)

        return APIResponse(
            success=True,
            data=response
        )

    def unmark_paid(self, expense_id: int, payer_id: int, requester_id: int) -> APIResponse:
        """
        Removes the payment mark for a specific user on an expense.
        """
        self._validate_permissions(expense_id, requester_id)
        self.repo.remove(expense_id, payer_id)

        return APIResponse(
            success=True,
            message="Payment removed."
        )

    def get_payments(self, expense_id: int, requester_id: int) -> APIResponse:
        """
        Retrieves all users who marked the expense as paid.
        """
        self._validate_permissions(expense_id, requester_id)

        payments = self.repo.get_all_by_expense(expense_id)
        response = [
            ExpensePaymentResponse.model_validate(p)
            for p in payments
        ]

        return APIResponse(
            success=True,
            data=response
        )
