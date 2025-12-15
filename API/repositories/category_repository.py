from abc import ABC, abstractmethod
from typing import List

from models.category import Category
from sqlalchemy import ARRAY, Text, asc, cast, desc, or_, select
from sqlalchemy.orm import Session


class ICategoryRepository(ABC):
    @abstractmethod
    def add(self, category: Category) -> int: ...

    @abstractmethod
    def get_all(self, sort_by: str, order: str) -> List[Category]: ...

    @abstractmethod
    def get_by_id(self, category_id: int) -> Category: ...

    @abstractmethod
    def update(self, category_id: int, fields: dict) -> int: ...

    @abstractmethod
    def delete(self, category_id: int) -> None: ...
    
    @abstractmethod
    def get_by_title_or_keywords(self, user_id: int, title: str, keywords: list[str]) -> bool: ...
    
    @abstractmethod
    def get_by_user(self, user_id: int) -> List[Category]: ...

class CategoryRepository(ICategoryRepository):
    def __init__(self, db: Session):
        self.db = db

    def add(self, category: Category) -> int:
        self.db.add(category)
        self.db.commit()
        self.db.refresh(category)
        return category.id

    def get_by_title_or_keywords(self, user_id: int, title: str, keywords: list[str]) -> bool:
        statement = (select(Category.id).where(Category.user_id == user_id,or_(
                    Category.title == title,
                    Category.keywords.op("&&")(cast(keywords, ARRAY(Text))))))
        return self.db.scalar(statement) is not None

    def get_all(self, sort_by: str, order: str) -> List[Category]:
        sort_column = getattr(Category, sort_by, Category.title)
        sort_order = asc(sort_column) if order == "asc" else desc(sort_column)
        statement = select(Category).order_by(sort_order)
        return list(self.db.scalars(statement))

    def get_by_id(self, category_id: int) -> Category:
        statement = select(Category).where(Category.id == category_id)
        return self.db.scalars(statement).first()
    
    def get_by_user(self, user_id: int) -> List[Category]: 
        statement = select(Category).where(Category.user_id == user_id)
        
        return list(self.db.scalars(statement))

    def update(self, category_id: int, fields: dict) -> int:
        category = self.get_by_id(category_id)
        for key, value in fields.items():
            if hasattr(category, key):
                setattr(category, key, value)
        self.db.commit()
        self.db.refresh(category)
        return category_id

    def delete(self, category_id: int) -> None:
        category = self.get_by_id(category_id)
        self.db.delete(category)
        self.db.commit()
