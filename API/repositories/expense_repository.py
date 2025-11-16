from abc import ABC, abstractmethod
from datetime import datetime
from typing import List, Optional

from models.expense import Expense
from models.users_groups import UsersGroups  # MODIFIED
from sqlalchemy import and_, asc, desc, or_, select  # MODIFIED
from sqlalchemy.orm import Session


class IExpenseRepository(ABC):
    # CREATE
    @abstractmethod
    def add(self, expense: Expense) -> None: ...

    # READ
    @abstractmethod
    def get_by_id(self, expense_id: int) -> Optional[Expense]: ...
    @abstractmethod
    def get_all(
        self, 
        offset: int, 
        limit: int, 
        sort_by: str = "created_at", 
        order: str = "desc",
        min_price: Optional[float] = None,
        max_price: Optional[float] = None,
        date_from: Optional[datetime] = None,
        date_to: Optional[datetime] = None,
        category: Optional[str] = None
    ) -> List[Expense]: ...
    @abstractmethod
    def get_by_user(
        self, 
        user_id: int, 
        offset: int, 
        limit: int, 
        sort_by: str = "created_at", 
        order: str = "desc",
        min_price: Optional[float] = None,
        max_price: Optional[float] = None,
        date_from: Optional[datetime] = None,
        date_to: Optional[datetime] = None,
        category: Optional[str] = None,
        group_ids: Optional[List[int]] = None # MODIFIED
    ) -> List[Expense]: ...
    @abstractmethod
    def get_by_group(self, group_id: int, offset: int, limit: int) -> List[Expense]: ...

    # UPDATE
    @abstractmethod
    def update(self, expense_id: int, fields: dict) -> None: ...

    # DELETE
    @abstractmethod
    def delete(self, expense_id: int) -> None: ...


class ExpenseRepository(IExpenseRepository):
    def __init__(self, db: Session):
        self.db = db

    def add(self, expense: Expense) -> Expense:
        self.db.add(expense)
        self.db.commit()
        self.db.refresh(expense)
        return expense

    def get_by_id(self, expense_id: int) -> Optional[Expense]:
        stmt = select(Expense).where(Expense.id == expense_id)
        return self.db.scalars(stmt).first()

    def get_all(
        self, 
        offset: int, 
        limit: int, 
        sort_by: str = "created_at", 
        order: str = "desc",
        min_price: Optional[float] = None,
        max_price: Optional[float] = None,
        date_from: Optional[datetime] = None,
        date_to: Optional[datetime] = None,
        category: Optional[str] = None
    ) -> List[Expense]:
        sort_column = getattr(Expense, sort_by, Expense.created_at)
        sort_order = desc(sort_column) if order.lower() == "desc" else asc(sort_column)
        
        # Build where conditions
        conditions = []
        
        if min_price is not None:
            conditions.append(Expense.amount >= min_price)
        if max_price is not None:
            conditions.append(Expense.amount <= max_price)
        if date_from is not None:
            conditions.append(Expense.created_at >= date_from)
        if date_to is not None:
            conditions.append(Expense.created_at <= date_to)
        if category is not None:
            conditions.append(Expense.category == category)
        
        stmt = select(Expense)
        if conditions:
            stmt = stmt.where(and_(*conditions))
        stmt = stmt.order_by(sort_order).offset(offset).limit(limit)
        
        return list(self.db.scalars(stmt))

    def get_by_user(
        self, 
        user_id: int, 
        offset: int, 
        limit: int, 
        sort_by: str = "created_at", 
        order: str = "desc",
        min_price: Optional[float] = None,
        max_price: Optional[float] = None,
        date_from: Optional[datetime] = None,
        date_to: Optional[datetime] = None,
        category: Optional[str] = None,
        group_ids: Optional[List[int]] = None # MODIFIED
    ) -> List[Expense]:
        sort_column = getattr(Expense, sort_by, Expense.created_at)
        sort_order = desc(sort_column) if order.lower() == "desc" else asc(sort_column)
        
        # Get all group IDs for the user
        user_group_ids_stmt = select(UsersGroups.group_id).where(UsersGroups.user_id == user_id)
        user_group_ids = list(self.db.scalars(user_group_ids_stmt))
        
        conditions = []

        if group_ids:
            # User is filtering by specific groups.
            # Only include groups they are actually a member of.
            allowed_filter_ids = [gid for gid in group_ids if gid in user_group_ids]
            if not allowed_filter_ids:
                conditions.append(Expense.group_id.in_([])) # Will return no results
            else:
                conditions.append(Expense.group_id.in_(allowed_filter_ids))
        else:
            # No group filter. Show personal expenses OR expenses from any group user is in.
            base_conditions = [
                Expense.user_id == user_id,
                Expense.group_id.in_(user_group_ids)
            ]
            conditions.append(or_(*base_conditions))
        
        # Add other filters
        if min_price is not None:
            conditions.append(Expense.amount >= min_price)
        if max_price is not None:
            conditions.append(Expense.amount <= max_price)
        if date_from is not None:
            conditions.append(Expense.created_at >= date_from)
        if date_to is not None:
            conditions.append(Expense.created_at <= date_to)
        if category is not None:
            conditions.append(Expense.category == category)
        
        stmt = (
            select(Expense)
            .where(and_(*conditions))
            .order_by(sort_order)
            .offset(offset)
            .limit(limit)
        )
        return list(self.db.scalars(stmt))

    def get_by_group(self, group_id: int, offset: int, limit: int, sort_by: str = "created_at", order: str = "desc") -> List[Expense]:
        sort_column = getattr(Expense, sort_by, Expense.created_at)
        sort_order = desc(sort_column) if order.lower() == "desc" else asc(sort_column)
        stmt = (
            select(Expense)
            .where(Expense.group_id == group_id)
            .order_by(sort_order)
            .offset(offset)
            .limit(limit)
        )
        return list(self.db.scalars(stmt))

    def update(self, expense_id: int, fields: dict) -> None:
        expense = self.get_by_id(expense_id)
        for key, value in fields.items():
            if hasattr(expense, key):
                setattr(expense, key, value)
        self.db.commit()
        self.db.refresh(expense)

    def delete(self, expense_id: int) -> None:
        expense = self.get_by_id(expense_id)
        self.db.delete(expense)
        self.db.commit()