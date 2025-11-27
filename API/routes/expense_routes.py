from typing import List, Optional

from dependencies.di import get_expense_service
from fastapi import APIRouter, Depends, Query, Request
from schemas.expense import ExpenseCreate, ExpenseUpdate
from services.expense_service import IExpenseService
from utils.helpers.convert_datetime_string import parse_date_string
from utils.helpers.jwt_utils import JwtUtils

router = APIRouter(tags=["Expenses"])


def get_current_user_id(request: Request) -> int:
    """
    Returns the authenticated user id.
    """
    return JwtUtils.auth_wrapper(request)

@router.post("/")
def create_expense(
    expense_in: ExpenseCreate,
    user_id: int = Depends(get_current_user_id),
    expense_service: IExpenseService = Depends(get_expense_service)
):
    """
    Method for creating a new expense (user_id taken from JWT, not request body)
    """
    return expense_service.create_expense(expense_in, user_id)


@router.get("/all")
def get_all_expenses(
    offset: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    sort_by: str = Query("created_at"),
    order: str = Query("desc", regex="^(asc|desc)$"),
    min_price: Optional[float] = Query(None, ge=0),
    max_price: Optional[float] = Query(None, ge=0),
    date_from: Optional[str] = Query(None),
    date_to: Optional[str] = Query(None),
    category: Optional[str] = Query(None),
    expense_service: IExpenseService = Depends(get_expense_service)
):
    """
    Retrieves all expenses in the system with filtering.
    """
    date_from_dt = parse_date_string(date_from)
    date_to_dt = parse_date_string(date_to)

    return expense_service.get_all_expenses(
        offset, limit, sort_by, order,
        min_price, max_price, date_from_dt, date_to_dt, category
    )

@router.get("/{expense_id}")
def get_expense(
    expense_id: int,
    expense_service: IExpenseService = Depends(get_expense_service)
):
    """
    Retrieves an expense by id.
    """
    return expense_service.get_expense_by_id(expense_id)


@router.get("/")
def get_user_expenses(
    user_id: int = Depends(get_current_user_id),
    offset: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    sort_by: str = Query("created_at"),
    order: str = Query("desc", regex="^(asc|desc)$"),
    min_price: Optional[float] = Query(None, ge=0),
    max_price: Optional[float] = Query(None, ge=0),
    date_from: Optional[str] = Query(None),
    date_to: Optional[str] = Query(None),
    category: Optional[str] = Query(None),
    group_ids: Optional[List[int]] = Query(None),
    expense_service: IExpenseService = Depends(get_expense_service)
):
    """
    Retrieves expenses for the authenticated user with filtering.
    """
    date_from_dt = parse_date_string(date_from)
    date_to_dt = parse_date_string(date_to)

    return expense_service.get_user_expenses(
        user_id, offset, limit, sort_by, order,
        min_price, max_price, date_from_dt, date_to_dt,
        category, group_ids
    )


@router.get("/group/{group_id}")
def get_group_expenses(
    group_id: int,
    offset: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    sort_by: str = Query("created_at"),
    order: str = Query("desc", regex="^(asc|desc)$"),
    min_price: Optional[float] = Query(None, ge=0),
    max_price: Optional[float] = Query(None, ge=0),
    date_from: Optional[str] = Query(None),
    date_to: Optional[str] = Query(None),
    category: Optional[str] = Query(None),
    expense_service: IExpenseService = Depends(get_expense_service)
):
    """
    Retrieves expenses for a group with filtering.
    """
    date_from_dt = parse_date_string(date_from)
    date_to_dt = parse_date_string(date_to)

    return expense_service.get_group_expenses(
        group_id, offset, limit, sort_by, order,
        min_price, max_price, date_from_dt, date_to_dt, category
    )


@router.put("/{expense_id}")
def update_expense(
    expense_id: int,
    expense_in: ExpenseUpdate,
    requester_id: int = Depends(get_current_user_id),
    expense_service: IExpenseService = Depends(get_expense_service)
):
    """
    Update allowed only if requester is the author.
    """
    return expense_service.update_expense(expense_id, expense_in, requester_id)


@router.delete("/{expense_id}")
def delete_expense(
    expense_id: int,
    requester_id: int = Depends(get_current_user_id),
    expense_service: IExpenseService = Depends(get_expense_service)
):
    """
    Delete allowed only if requester is the author.
    """
    return expense_service.delete_expense(expense_id, requester_id)
