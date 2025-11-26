from fastapi import Depends
from sqlalchemy.orm import Session

from database import get_db
from repositories.user_repository import UserRepository
from services.user_service import UserService, IUserService


def get_user_repository(db: Session = Depends(get_db)):
    return UserRepository(db)


def get_user_service(
    repo: UserRepository = Depends(get_user_repository)
) -> IUserService:
    return UserService(repo)
