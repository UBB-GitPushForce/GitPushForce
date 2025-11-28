from database import get_db
from fastapi import Depends
from repositories.expense_payment_repository import ExpensePaymentRepository, IExpensePaymentRepository
from repositories.expense_repository import ExpenseRepository, IExpenseRepository
from repositories.group_log_repository import GroupLogRepository, IGroupLogRepository
from repositories.group_repository import GroupRepository, IGroupRepository
from repositories.user_group_repository import IUserGroupRepository, UserGroupRepository
from repositories.user_repository import IUserRepository, UserRepository
from services.expense_payment_service import ExpensePaymentService, IExpensePaymentService
from services.expense_service import ExpenseService, IExpenseService
from services.group_log_service import GroupLogService, IGroupLogService
from services.group_service import GroupService, IGroupService
from services.user_group_service import IUserGroupService, UserGroupService
from services.user_service import IUserService, UserService
from sqlalchemy.orm import Session


def get_user_repository(db: Session = Depends(get_db)) -> IUserRepository:
    return UserRepository(db)

def get_user_service(repo: IUserRepository = Depends(get_user_repository)) -> IUserService:
    return UserService(repo)

    
def get_group_repository(db: Session = Depends(get_db)) -> IGroupRepository:
    return GroupRepository(db)

def get_group_service(repo: IGroupRepository = Depends(get_group_repository)) -> IGroupService:
    return GroupService(repo)

def get_expense_repository(db: Session = Depends(get_db)) -> IExpenseRepository:
    return ExpenseRepository(db)

def get_expense_service(
    repo: IExpenseRepository = Depends(get_expense_repository),
    group_repo: IGroupRepository = Depends(get_group_repository),
) -> IExpenseService:
    return ExpenseService(repo, group_repo)

def get_user_group_repository(db: Session = Depends(get_db)) -> IUserGroupRepository:
    return UserGroupRepository(db)

def get_user_group_service(
    user_group_repo: IUserGroupRepository = Depends(get_user_group_repository),
    group_repo: IGroupRepository = Depends(get_group_repository),
    user_repo: IUserRepository = Depends(get_user_repository),
) -> IUserGroupService:
    return UserGroupService(user_group_repo, group_repo, user_repo)

def get_group_log_repository(db: Session = Depends(get_db)) -> IGroupLogRepository:
    return GroupLogRepository(db)

def get_group_log_service(
    group_log_repo: IGroupLogRepository = Depends(get_group_log_repository),
    group_repo: IGroupRepository = Depends(get_group_repository),
    user_repo: IUserRepository = Depends(get_user_repository),
) -> IGroupLogService:
    return GroupLogService(group_log_repo, group_repo, user_repo)

def get_expense_payment_repository(db: Session = Depends(get_db)) -> IExpensePaymentRepository:
    return ExpensePaymentRepository(db)

def get_expense_payment_service(
    repo: IExpensePaymentRepository = Depends(get_expense_payment_repository),
    expense_repository: IExpenseRepository = Depends(get_expense_repository),
    group_repository: IGroupRepository = Depends(get_group_repository),
    user_repository: IUserRepository = Depends(get_user_repository)
) -> IExpensePaymentService:
    return ExpensePaymentService(repo, expense_repository, group_repository, user_repository)