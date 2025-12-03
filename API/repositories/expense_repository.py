from abc import ABC, abstractmethod
from datetime import datetime
from typing import List, Optional

from models.category import Category
from models.expense import Expense
from models.user_group import UserGroup
from models.group import Group
from sqlalchemy import and_, asc, desc, or_, select
from sqlalchemy.orm import Session, joinedload


class IExpenseRepository(ABC):
    @abstractmethod
    def add(self, expense: Expense) -> int: ...
    
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
        group_ids: Optional[List[int]] = None
    ) -> List[Expense]: ...
    
    @abstractmethod
    def get_by_group(
        self, 
        group_id: int, 
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
    def update(self, expense_id: int, fields: dict) -> int: ...
    
    @abstractmethod
    def delete(self, expense_id: int) -> int: ...


class ExpenseRepository(IExpenseRepository):
    def __init__(self, db: Session):
        self.db = db

    def _resolve_category_ids(self, category_name: str) -> list[int]:
        stmt = select(Category.id).where(Category.title.ilike(category_name))
        return list(self.db.scalars(stmt))

    def add(self, expense: Expense) -> int:
        """
        Method for adding a new expense.
        """
        self.db.add(expense)
        self.db.commit()
        self.db.refresh(expense)
        
        return expense.id

    def get_by_id(self, expense_id: int) -> Optional[Expense]:
        """
        Method for retrieving expense by id.
        """
        statement = select(Expense).where(Expense.id == expense_id)
        
        return self.db.scalars(statement).unique().first()

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
        """
        Method for retrieving expenses with pagination sorting and filtering.
        """
        sort_column = getattr(Expense, sort_by, Expense.created_at)
        sort_order = desc(sort_column) if order.lower() == "desc" else asc(sort_column)
        
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
            category_ids = self._resolve_category_ids(category)
            if category_ids:
                conditions.append(Expense.category_id.in_(category_ids))
        
        statement = select(Expense).options(
            joinedload(Expense.group).joinedload(Group.users)
        )
        if conditions:
            statement = statement.where(and_(*conditions))

        statement = statement.order_by(sort_order).offset(offset).limit(limit)
        return list(self.db.scalars(statement).unique())

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
        group_ids: Optional[List[int]] = None
    ) -> List[Expense]:
        """
        Method for retrieving expenses for a user with optional group and filter rules.
        """
        sort_column = getattr(Expense, sort_by, Expense.created_at)
        sort_order = desc(sort_column) if order.lower() == "desc" else asc(sort_column)
        
        user_group_ids_statement = select(UserGroup.group_id).where(UserGroup.user_id == user_id)
        user_group_ids = list(self.db.scalars(user_group_ids_statement))
        
        conditions = []

        if group_ids:
            allowed_filter_ids = [group_id for group_id in group_ids if group_id in user_group_ids]
            if not allowed_filter_ids:
                return []
            
            conditions.append(Expense.group_id.in_(allowed_filter_ids))
        else:
            base_conditions = [
                Expense.user_id == user_id,
                Expense.group_id.in_(user_group_ids)
            ]
            conditions.append(or_(*base_conditions))
        
        if min_price is not None:
            conditions.append(Expense.amount >= min_price)
        if max_price is not None:
            conditions.append(Expense.amount <= max_price)
        if date_from is not None:
            conditions.append(Expense.created_at >= date_from)
        if date_to is not None:
            conditions.append(Expense.created_at <= date_to)
        if category is not None:
            category_ids = self._resolve_category_ids(category)
            if category_ids:
                conditions.append(Expense.category_id.in_(category_ids))
        
        statement = (
            select(Expense)
            .options(joinedload(Expense.group).joinedload(Group.users))
            .where(and_(*conditions))
            .order_by(sort_order)
            .offset(offset)
            .limit(limit)
        )
        
        return list(self.db.scalars(statement).unique())

    def get_by_group(
        self, 
        group_id: int, 
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
        """
        Method for retrieving expenses for a group with pagination and filters.
        """
        sort_column = getattr(Expense, sort_by, Expense.created_at)
        sort_order = desc(sort_column) if order.lower() == "desc" else asc(sort_column)
        
        conditions = [Expense.group_id == group_id] 
        
        if min_price is not None:
            conditions.append(Expense.amount >= min_price)
        if max_price is not None:
            conditions.append(Expense.amount <= max_price)
        if date_from is not None:
            conditions.append(Expense.created_at >= date_from)
        if date_to is not None:
            conditions.append(Expense.created_at <= date_to)
        if category is not None:
            category_ids = self._resolve_category_ids(category)
            if category_ids:
                conditions.append(Expense.category_id.in_(category_ids))
            
        statement = (
            select(Expense)
            .options(joinedload(Expense.group).joinedload(Group.users))
            .where(and_(*conditions)) 
            .order_by(sort_order)
            .offset(offset)
            .limit(limit)
        )
        
        return list(self.db.scalars(statement).unique())

    def update(self, expense_id: int, fields: dict) -> int:
        """
        Method for updating specific fields of an expense.
        """
        expense = self.get_by_id(expense_id)
        for key, value in fields.items():
            if hasattr(expense, key):
                setattr(expense, key, value)
                
        self.db.commit()
        self.db.refresh(expense)
        
        return expense_id

    def delete(self, expense_id: int) -> int:
        """
        Method for removing an expense by id.
        """
        expense = self.get_by_id(expense_id)
        self.db.delete(expense)
        self.db.commit()
        
        return expense_id