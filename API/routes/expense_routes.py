from datetime import datetime
from typing import List, Optional

from database import get_db
from fastapi import APIRouter, Depends, HTTPException, Query, Request, status
from repositories.expense_repository import ExpenseRepository
from schemas.expense import Expense, ExpenseCreate, ExpenseUpdate
from services.expense_service import ExpenseService
from services.user_service import UserService
from sqlalchemy.exc import NoResultFound
from sqlalchemy.orm import Session

router = APIRouter(tags=["Expenses"])


def get_current_user_id(request: Request, db: Session = Depends(get_db)) -> int:
    """
    Returns the authenticated user id.
    """
    service = UserService(db)
    return service.auth_wrapper(request)


def get_expense_service(db: Session = Depends(get_db)) -> ExpenseService:
    """
    Returns an expense service instance.
    """
    repo = ExpenseRepository(db)
    return ExpenseService(repo, db)  # 🔥 FIX: pass db into service


def parse_date_string(date_str: Optional[str]) -> Optional[datetime]:
    """
    Converts a date string into a datetime object.
    """
    if not date_str:
        return None
    
    try:
        return datetime.fromisoformat(date_str.replace('Z', '+00:00'))
    except ValueError:
        try:
            return datetime.strptime(date_str, "%Y-%m-%d")
        except ValueError:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Invalid date format: {date_str}. Use YYYY-MM-DD or YYYY-MM-DDTHH:MM:SS"
            )


@router.post("/", response_model=Expense, status_code=201)
def create_expense(
    expense_in: ExpenseCreate,
    db: Session = Depends(get_db),
    user_id: int = Depends(get_current_user_id),
):
    """
    Creates a new expense (user_id taken from JWT, not request body)
    """
    service = ExpenseService(ExpenseRepository(db), db)
    return service.create_expense(expense_in, user_id)


@router.get("/all", response_model=List[Expense])
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
    service: ExpenseService = Depends(get_expense_service)
):
    """
    Retrieves all expenses in the system with filtering.
    """
    date_from_dt = parse_date_string(date_from)
    date_to_dt = parse_date_string(date_to)

    return service.get_all_expenses(
        offset, limit, sort_by, order,
        min_price, max_price, date_from_dt, date_to_dt, category
    )


@router.get("/{expense_id}", response_model=Expense)
def get_expense(
    expense_id: int,
    service: ExpenseService = Depends(get_expense_service)
):
    """
    Retrieves an expense by id.
    """
    try:
        return service.get_expense_by_id(expense_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))


@router.get("/", response_model=List[Expense])
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
    service: ExpenseService = Depends(get_expense_service)
):
    """
    Retrieves expenses for the authenticated user with filtering.
    """
    date_from_dt = parse_date_string(date_from)
    date_to_dt = parse_date_string(date_to)

    return service.get_user_expenses(
        user_id, offset, limit, sort_by, order,
        min_price, max_price, date_from_dt, date_to_dt,
        category, group_ids
    )


@router.get("/group/{group_id}", response_model=List[Expense])
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
    service: ExpenseService = Depends(get_expense_service)
):
    """
    Retrieves expenses for a group with filtering.
    """
    date_from_dt = parse_date_string(date_from)
    date_to_dt = parse_date_string(date_to)

    return service.get_group_expenses(
        group_id, offset, limit, sort_by, order,
        min_price, max_price, date_from_dt, date_to_dt, category
    )


@router.put("/{expense_id}")
def update_expense(
    expense_id: int,
    expense_in: ExpenseUpdate,
    db: Session = Depends(get_db),
    requester_id: int = Depends(get_current_user_id),
):
    """
    Update allowed only if requester is the author.
    """
    service = ExpenseService(ExpenseRepository(db), db)
    service.update_expense(expense_id, expense_in, requester_id)
    return {"message": "Expense updated successfully"}


@router.delete("/{expense_id}", status_code=204)
def delete_expense(
    expense_id: int,
    db: Session = Depends(get_db),
    requester_id: int = Depends(get_current_user_id),
):
    """
    Delete allowed only if requester is the author.
    """
    service = ExpenseService(ExpenseRepository(db), db)
    service.delete_expense(expense_id, requester_id)
    return None
