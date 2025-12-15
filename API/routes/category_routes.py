from dependencies.di import get_category_service
from fastapi import APIRouter, Depends, Request
from fastapi.params import Query
from schemas.category import CategoryCreate, CategoryUpdate
from services.category_service import ICategoryService
from utils.helpers.jwt_utils import JwtUtils
from utils.helpers.logger import Logger

router = APIRouter(tags=["Categories"])
logger = Logger()

def get_current_user_id(request: Request) -> int:
    """
    Returns the authenticated user id.
    """
    return JwtUtils.auth_wrapper(request)

@router.post("/")
def create_category(category_in: CategoryCreate, user_id: int = Depends(get_current_user_id), category_service: ICategoryService = Depends(get_category_service)):
    return category_service.create_category(category_in, user_id)

@router.get("/")
def get_all_categories(
        _ = Depends(get_current_user_id),
        category_service: ICategoryService = Depends(get_category_service),
        sort_by: str = Query("title"),
        order: str = Query("asc", regex="^(asc|desc)$")
):
    return category_service.get_all_categories(sort_by, order)

@router.get("/{user_id}")
def get_categories_by_user(
    user_id = Depends(get_current_user_id),
    category_service: ICategoryService = Depends(get_category_service)
):
    return category_service.get_categories_by_user(user_id)

@router.put("/{category_id}")
def update_category(category_id: int, category_in: CategoryUpdate, requester_id: int = Depends(get_current_user_id), category_service: ICategoryService = Depends(get_category_service)):
    return category_service.update_category(category_id, category_in, requester_id)

@router.delete("/{category_id}")
def delete_category(category_id: int, requester_id: int = Depends(get_current_user_id), category_service: ICategoryService = Depends(get_category_service)):
    return category_service.delete_category(category_id, requester_id)



