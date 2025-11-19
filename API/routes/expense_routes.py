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

    Args:
        request (Request) incoming request with authentication data
        db (Session) database session

    Returns:
        int id of the authenticated user

    Exceptions:
        HTTPException returned when authentication fails
    """
    service = UserService(db)
    return service.auth_wrapper(request)


def get_expense_service(db: Session = Depends(get_db)) -> ExpenseService:
    """
    Returns an expense service instance.

    Args:
        db (Session) database session

    Returns:
        ExpenseService service used for expense operations

    Exceptions:
        None
    """
    repo = ExpenseRepository(db)
    return ExpenseService(repo)


def parse_date_string(date_str: Optional[str]) -> Optional[datetime]:
    """
    Converts a date string into a datetime object.

    Args:
        date_str (str) date text in ISO or YYYY-MM-DD format

    Returns:
        datetime or None parsed datetime or None

    Exceptions:
        HTTPException returned when format is invalid
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
    service: ExpenseService = Depends(get_expense_service)
):
    """
    Creates a new expense.

    Args:
        expense_in (ExpenseCreate) expense data to create
        service (ExpenseService) expense service instance

    Returns:
        Expense created expense object

    Exceptions:
        HTTPException returned when creation fails
    """
    try:
        return service.create_expense(expense_in)
    except Exception as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))


@router.get("/all", response_model=List[Expense])
def get_all_expenses(
    offset: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    sort_by: str = Query("created_at"),
    order: str = Query("desc", regex="^(asc|desc)$"),
    min_price: Optional[float] = Query(None, ge=0, description="Minimum price filter"),
    max_price: Optional[float] = Query(None, ge=0, description="Maximum price filter"),
    date_from: Optional[str] = Query(None, description="Filter expenses from this date (ISO format: YYYY-MM-DD or YYYY-MM-DDTHH:MM:SS)"),
    date_to: Optional[str] = Query(None, description="Filter expenses until this date (ISO format: YYYY-MM-DD or YYYY-MM-DDTHH:MM:SS)"),
    category: Optional[str] = Query(None, description="Filter by category"),
    service: ExpenseService = Depends(get_expense_service)
):
    """
    Retrieves all expenses in the system with filtering.

    Args:
        offset (int) items to skip
        limit (int) maximum items to return
        sort_by (str) field used for sorting
        order (str) sort direction
        min_price (float) minimum amount filter
        max_price (float) maximum amount filter
        date_from (str) start date filter
        date_to (str) end date filter
        category (str) category filter
        service (ExpenseService) expense service instance

    Returns:
        list[Expense] filtered expenses for all users

    Exceptions:
        HTTPException returned when inputs are invalid
    """
    date_from_dt = parse_date_string(date_from)
    date_to_dt = parse_date_string(date_to)
    
    if min_price is not None and max_price is not None and min_price > max_price:
        raise HTTPException(
            status_code=status.http_400_bad_request,
            detail="min_price cannot be greater than max_price"
        )
    
    return service.get_all_expenses(
        offset, 
        limit, 
        sort_by, 
        order,
        min_price,
        max_price,
        date_from_dt,
        date_to_dt,
        category
    )


@router.get("/{expense_id}", response_model=Expense)
def get_expense(
    expense_id: int,
    service: ExpenseService = Depends(get_expense_service)
):
    """
    Retrieves an expense by id.

    Args:
        expense_id (int) id of the expense
        service (ExpenseService) expense service instance

    Returns:
        Expense matching expense object

    Exceptions:
        HTTPException returned when expense is not found
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
    min_price: Optional[float] = Query(None, ge=0, description="Minimum price filter"),
    max_price: Optional[float] = Query(None, ge=0, description="Maximum price filter"),
    date_from: Optional[str] = Query(None, description="Filter expenses from this date (ISO format: YYYY-MM-DD or YYYY-MM-DDTHH:MM:SS)"),
    date_to: Optional[str] = Query(None, description="Filter expenses until this date (ISO format: YYYY-MM-DD or YYYY-MM-DDTHH:MM:SS)"),
    category: Optional[str] = Query(None, description="Filter by category"),
    group_ids: Optional[List[int]] = Query(None, description="Filter by one or more group IDs"),
    service: ExpenseService = Depends(get_expense_service)
):
    """
    Retrieves expenses for the authenticated user with filtering.

    Args:
        user_id (int) authenticated user id
        offset (int) items to skip
        limit (int) maximum items to return
        sort_by (str) field used for sorting
        order (str) sort direction
        min_price (float) minimum amount filter
        max_price (float) maximum amount filter
        date_from (str) start date filter
        date_to (str) end date filter
        category (str) category filter
        group_ids (list[int]) list of group ids to filter
        service (ExpenseService) expense service instance

    Returns:
        list[Expense] filtered user expenses

    Exceptions:
        HTTPException returned when inputs are invalid
    """
    date_from_dt = parse_date_string(date_from)
    date_to_dt = parse_date_string(date_to)
    
    if min_price is not None and max_price is not None and min_price > max_price:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="min_price cannot be greater than max_price"
        )
    
    return service.get_user_expenses(
        user_id, 
        offset, 
        limit, 
        sort_by, 
        order,
        min_price,
        max_price,
        date_from_dt,
        date_to_dt,
        category,
        group_ids
    )


@router.get("/group/{group_id}", response_model=List[Expense])
def get_group_expenses(
    group_id: int,
    offset: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    sort_by: str = Query("created_at"),
    order: str = Query("desc", regex="^(asc|desc)$"),
    min_price: Optional[float] = Query(None, ge=0, description="Minimum price filter"),
    max_price: Optional[float] = Query(None, ge=0, description="Maximum price filter"),
    date_from: Optional[str] = Query(None, description="Filter expenses from this date (ISO format: YYYY-MM-DD or YYYY-MM-DDTHH:MM:SS)"),
    date_to: Optional[str] = Query(None, description="Filter expenses until this date (ISO format: YYYY-MM-DD or YYYY-MM-DDTHH:MM:SS)"),
    category: Optional[str] = Query(None, description="Filter by category"),
    service: ExpenseService = Depends(get_expense_service)
):
    """
    Retrieves expenses for a group with filtering.

    Args:
        group_id (int) id of the group
        offset (int) items to skip
        limit (int) maximum items to return
        sort_by (str) field used for sorting
        order (str) sort direction
        min_price (float) minimum amount filter
        max_price (float) maximum amount filter
        date_from (str) start date filter
        date_to (str) end date filter
        category (str) category filter
        service (ExpenseService) expense service instance

    Returns:
        list[Expense] filtered group expenses

    Exceptions:
        HTTPException returned when inputs are invalid
    """
    date_from_dt = parse_date_string(date_from)
    date_to_dt = parse_date_string(date_to)
    
    if min_price is not None and max_price is not None and min_price > max_price:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="min_price cannot be greater than max_price"
        )
    
    return service.get_group_expenses(
        group_id,
        offset, 
        limit, 
        sort_by, 
        order,
        min_price,
        max_price,
        date_from_dt,
        date_to_dt,
        category
    )


@router.put("/{expense_id}")
def update_expense(
    expense_id: int,
    expense_in: ExpenseUpdate,
    service: ExpenseService = Depends(get_expense_service)
):
    """
    Updates an expense by id.

    Args:
        expense_id (int) id of the expense
        expense_in (ExpenseUpdate) updated fields
        service (ExpenseService) expense service instance

    Returns:
        str confirmation of successful update

    Exceptions:
        HTTPException returned when expense is not found or data is invalid
    """
    try:
        service.update_expense(expense_id, expense_in)
        return "Successfull update"
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_44_NOT_FOUND, detail=str(e))
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))


@router.delete("/{expense_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_expense(
    expense_id: int,
    service: ExpenseService = Depends(get_expense_service)
):
    """
    Deletes an expense by id.

    Args:
        expense_id (int) id of the expense
        service (ExpenseService) expense service instance

    Returns:
        None no content returned

    Exceptions:
        HTTPException returned when expense is not found
    """
    try:
        service.delete_expense(expense_id)
    except NoResultFound as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))
