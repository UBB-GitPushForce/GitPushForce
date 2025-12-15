from abc import ABC, abstractmethod

from fastapi import HTTPException
from models.category import Category
from repositories.category_repository import ICategoryRepository
from schemas.api_response import APIResponse
from schemas.category import CategoryCreate, CategoryResponse, CategoryUpdate
from utils.helpers.constants import (
    ID_FIELD,
    STATUS_BAD_REQUEST,
    STATUS_FORBIDDEN,
    STATUS_NOT_FOUND,
)
from utils.helpers.logger import Logger


class ICategoryService(ABC):
    @abstractmethod
    def create_category(self, data: CategoryCreate, user_id: int) -> APIResponse: ...

    @abstractmethod
    def get_all_categories(self, sort_by: str, order: int) -> APIResponse: ...

    @abstractmethod
    def update_category(self, category_id: int, data: CategoryUpdate, requester_id: int) -> APIResponse: ...

    @abstractmethod
    def delete_category(self, category_id: int, requester_id: int) -> APIResponse: ...
    
    @abstractmethod
    def get_categories_by_user(self, user_id: int) -> APIResponse: ...

class CategoryService(ICategoryService):
    def __init__(self, repository: ICategoryRepository):
        self.logger = Logger()
        self.repository = repository

    def _validate_category(self, category_id: int) -> Category:
        category = self.repository.get_by_id(category_id)
        if not category:
            raise HTTPException(status_code=STATUS_NOT_FOUND, detail=f"Category with id {category_id} not found")
        return category

    def _validate_owner(self, category_id: int, requester_id: int) -> Category:
        category = self._validate_category(category_id)
        if category.user_id != requester_id:
            raise HTTPException(status_code=STATUS_FORBIDDEN, detail="Not allowed to modify this category.")
        return category

    def create_category(self, data: CategoryCreate, user_id: int) -> APIResponse:
        self.logger.info(f"Creating a category with title {data.title} for user with id {user_id}")
        if self.repository.get_by_title_or_keywords(user_id, data.title, data.keywords):
            raise HTTPException(
                status_code=400,
                detail="Category with same title or overlapping keywords already exists."
            )
        category = Category(
            user_id = user_id,
            title = data.title,
            keywords = data.keywords
        )
        id = self.repository.add(category)
        return APIResponse(
            success=True,
            data={
                ID_FIELD: id
            }
        )

    def get_all_categories(self, sort_by: str, order: str) -> APIResponse:
        self.logger.info("Fetching all categories")

        categories = self.repository.get_all(sort_by, order)
        categories_response = [CategoryResponse.model_validate(category) for category in categories]
        return APIResponse(
            success=True,
            data=categories_response
        )
        
    def get_categories_by_user(self, user_id: int) -> APIResponse:
        self.logger.info(f"Fetching categories for user with id {user_id}")
        categories = self.repository.get_by_user(user_id)
        
        return categories

    def update_category(self, category_id: int, data: CategoryUpdate, requester_id: int) -> APIResponse:
        self.logger.info(f"Updating category with id {category_id}")

        self._validate_owner(category_id, requester_id)
        fields = data.model_dump(exclude_unset=True)
        if not fields:
            raise HTTPException(status_code=STATUS_BAD_REQUEST, detail="No fields provided for update")
        id = self.repository.update(category_id, fields)
        return APIResponse(
            success=True,
            data={
                ID_FIELD: id
            }
        )

    def delete_category(self, category_id: int, requester_id: int) -> APIResponse:
        self.logger.info(f"Deleting category with id {category_id}")

        self._validate_owner(category_id, requester_id)
        self.repository.delete(category_id)
        return APIResponse(
            success=True
        )
