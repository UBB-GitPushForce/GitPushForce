from fastapi import APIRouter, Depends, HTTPException, status
from typing import List
from sqlalchemy.exc import NoResultFound

from database import get_db
from schemas.expense import ExpenseCreate, ExpenseUpdate, ExpenseBase, ExpenseBase
from services.expense_service import ExpenseService
from repositories.expense_repository import ExpenseRepository
from sqlalchemy.orm import Session# your session dependency

router = APIRouter(tags=["Expenses"])


# Temporary dependency to simulate current user
def get_current_user_id() -> int:
    # For now, just return a fixed user ID (e.g., 1)
    return 1


# Dependency to get the ExpenseService instance
def get_expense_service(db: Session = Depends(get_db)) -> ExpenseService:
    repo = ExpenseRepository(db)
    return ExpenseService(repo)


@router.post("/", response_model=ExpenseBase, status_code=201)
def create_expense(
    expense_in: ExpenseCreate,
    user_id: int = Depends(get_current_user_id),
    service: ExpenseService = Depends(get_expense_service)
):
    try:
        return service.create_expense(expense_in, user_id)
    except Exception as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))


@router.get("/{expense_id}", response_model=ExpenseBase)
def get_expense(
    expense_id: int,
    user_id: int = Depends(get_current_user_id),
    service: ExpenseService = Depends(get_expense_service)
):
    try:
        return service.get_expense(expense_id, user_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))


@router.get("/", response_model=List[ExpenseBase])
def get_user_expenses(
    user_id: int = Depends(get_current_user_id),
    offset: int = 0,
    limit: int = 100,
    service: ExpenseService = Depends(get_expense_service)
):
    return service.get_user_expenses(user_id, offset, limit)


@router.put("/{expense_id}", response_model=ExpenseBase)
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
