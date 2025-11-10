from typing import List

from database import get_db
from fastapi import APIRouter, Depends, HTTPException, Request, status
from repositories.expense_repository import ExpenseRepository
from schemas.expense import Expense, ExpenseCreate, ExpenseUpdate
from services.expense_service import ExpenseService
from services.user_service import UserService
from sqlalchemy.exc import NoResultFound
from sqlalchemy.orm import Session

router = APIRouter(tags=["Expenses"])


def get_current_user_id(request: Request, db: Session = Depends(get_db)) -> int:
    """
    Reads the JWT token from the request (header or cookie) and returns the user ID.
    """
    service = UserService(db)
    return service.auth_wrapper(request)


def get_expense_service(db: Session = Depends(get_db)) -> ExpenseService:
    repo = ExpenseRepository(db)
    return ExpenseService(repo)


@router.post("/", response_model=Expense, status_code=201)
def create_expense(
    expense_in: ExpenseCreate,
    user_id: int = Depends(get_current_user_id),
    service: ExpenseService = Depends(get_expense_service)
):
    try:
        return service.create_expense(expense_in)
    except Exception as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))


@router.get("/{expense_id}", response_model=Expense)
def get_expense(
    expense_id: int,
    user_id: int = Depends(get_current_user_id),
    service: ExpenseService = Depends(get_expense_service)
):
    try:
        return service.get_expense_by_id(expense_id, user_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))


@router.get("/", response_model=List[Expense])
def get_user_expenses(
    user_id: int = Depends(get_current_user_id),
    offset: int = 0,
    limit: int = 100,
    sort_by: str = "created_at",
    order: str = "desc",
    service: ExpenseService = Depends(get_expense_service)
):
    return service.get_user_expenses(user_id, offset, limit, sort_by, order)


@router.put("/{expense_id}", response_model=Expense)
def update_expense(
    expense_id: int,
    expense_in: ExpenseUpdate,
    user_id: int = Depends(get_current_user_id),
    service: ExpenseService = Depends(get_expense_service)
):
    try:
        return service.update_expense(expense_id, expense_in, user_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))


@router.delete("/{expense_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_expense(
    expense_id: int,
    user_id: int = Depends(get_current_user_id),
    service: ExpenseService = Depends(get_expense_service)
):
    try:
        service.delete_expense(expense_id, user_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))
