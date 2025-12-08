from abc import ABC, abstractmethod

from fastapi import HTTPException
from models.expense import Expense
from models.group import Group
from repositories.expense_repository import IExpenseRepository
from repositories.group_repository import IGroupRepository
from repositories.user_group_repository import IUserGroupRepository
from schemas.api_response import APIResponse
from schemas.expense import ExpenseCreate, ExpenseResponse, ExpenseUpdate
from utils.helpers.constants import ID_FIELD, STATUS_FORBIDDEN, STATUS_NOT_FOUND


class IExpenseService(ABC):
    """
    Interface for the expense service, achieves loose coupling.
    """
    @abstractmethod
    def create_expense(self, data: ExpenseCreate, user_id: int) -> APIResponse: ...
    
    @abstractmethod
    def get_expense_by_id(self, expense_id: int) -> APIResponse: ...
    
    @abstractmethod
    def get_all_expenses(self, *args, **kwargs) -> APIResponse: ...
    
    @abstractmethod
    def get_user_expenses(self, *args, **kwargs) -> APIResponse: ...
    
    @abstractmethod
    def get_group_expenses(self, *args, **kwargs) -> APIResponse: ...
    
    @abstractmethod
    def update_expense(self, expense_id: int, data: ExpenseUpdate, requester_id: int) -> APIResponse: ...
    
    @abstractmethod
    def delete_expense(self, expense_id: int, requester_id: int) -> APIResponse: ...

    @abstractmethod
    def get_user_group_statistics(self, user_id: int, group_id: int) -> APIResponse: ...


class ExpenseService(IExpenseService):
    """
    Implementation for the interface
    """
    def __init__(self, repository: IExpenseRepository, group_repository: IGroupRepository, user_group_repository: IUserGroupRepository):
        self.repository = repository
        self.group_repository = group_repository
        self.user_group_repository = user_group_repository

    def _validate_expense(self, expense_id: int) -> Expense:
        expense = self.repository.get_by_id(expense_id)
        if not expense:
            raise HTTPException(status_code=STATUS_NOT_FOUND, detail="Expense not found.")

        return expense

    def _validate_group(self, group_id: int) -> Group:
        group = self.group_repository.get_by_id(group_id)
        if not group:
            raise HTTPException(status_code=STATUS_NOT_FOUND, detail="Group not found.")
        
        return group

    def _validate_owner(self, expense_id: int, requester_id: int) -> Expense:
        """
        Internal method for validating user. So that each user interacts with their expenses.
        """
        expense = self._validate_expense(expense_id)
        if expense.user_id != requester_id:
            raise HTTPException(status_code=STATUS_FORBIDDEN, detail="Not allowed to modify this expense.")
        
        return expense

    def create_expense(self, data: ExpenseCreate, user_id: int) -> APIResponse:
        """
        Method for creating an expense
        """
        expense = Expense(
            **data.model_dump(),
            user_id=user_id
        )
        if data.group_id is not None:
            self._validate_group(data.group_id)
        
        id = self.repository.add(expense)
        
        return APIResponse(
            success=True,
            data={
                ID_FIELD: id
            }
        )

    def get_expense_by_id(self, expense_id: int) -> APIResponse:
        """
        Method for returning an expense by its id
        """
        expense = self._validate_expense(expense_id)
        
        expense_response = ExpenseResponse.model_validate(expense)
        
        return APIResponse(
            success=True,
            data=expense_response
        )

    def get_all_expenses(self, *args, **kwargs) -> APIResponse:
        """
        Method for returning all expenses
        """
        expenses = self.repository.get_all(*args, **kwargs)
        
        expenses_response = [ExpenseResponse.model_validate(expense) for expense in expenses]
        
        return APIResponse(
            success=True,
            data=expenses_response
        )

    def get_user_expenses(self, *args, **kwargs) -> APIResponse:
        """
        Method for returing user expenses
        """
        expenses = self.repository.get_by_user(*args, **kwargs)
        
        expenses_response = [ExpenseResponse.model_validate(expense) for expense in expenses]
        
        return APIResponse(
            success=True,
            data=expenses_response
        )

    def get_group_expenses(self, group_id: int, *args, **kwargs) -> APIResponse:
        """
        Method for returning group expenses
        """
        self._validate_group(group_id)
        
        expenses = self.repository.get_by_group(group_id, *args, **kwargs)
        
        expenses_response = [ExpenseResponse.model_validate(expense) for expense in expenses]
        
        return APIResponse(
            success=True,
            data=expenses_response
        )

    def update_expense(self, expense_id: int, data: ExpenseUpdate, requester_id: int) -> APIResponse:
        """
        Method for updating an expense. It checks that the user owns the expense.
        """
        self._validate_owner(expense_id, requester_id)
        
        fields = data.model_dump(exclude_unset=True)
        fields.pop("user_id", None)
        
        id = self.repository.update(expense_id, fields)
        
        return APIResponse(
            success=True,
            data={
                ID_FIELD: id
            }
        )

    def delete_expense(self, expense_id: int, requester_id: int) -> APIResponse:
        """
        Method for deleting an expense. It checks that the user owns the expense.
        """
        self._validate_owner(expense_id, requester_id)
        
        id = self.repository.delete(expense_id)
        
        return APIResponse(
            success=True,
            data={
                ID_FIELD: id
            }
        )
    
    def get_user_group_statistics(self, user_id: int, group_id: int) -> APIResponse:
        """
        Returns statistics for the authenticated user within a specific group.
        """
        self._validate_group(group_id)

        is_member = self.user_group_repository.is_member(user_id, group_id)
        if not is_member:
            raise HTTPException(status_code=STATUS_FORBIDDEN, detail="User is not a member of this group.")

        group_expenses = self.repository.get_by_group(group_id, offset=0, limit=100000)
        total_group_spend = sum(e.amount for e in group_expenses)

        my_expenses = [e for e in group_expenses if e.user_id == user_id]
        my_total_paid = sum(e.amount for e in my_expenses)
        
        rest_of_expenses = total_group_spend - my_total_paid
        member_count = self.user_group_repository.get_nr_of_users_from_group(group_id)

        my_share = 0
        if member_count > 0:
            my_share = total_group_spend / member_count
            
        paid_for_others = my_total_paid - my_share

        return APIResponse(
            success=True,
            data={
                "total_group_spend": total_group_spend,
                "my_total_paid": my_total_paid,
                "my_share_of_expenses": my_share, 
                "net_balance_paid_for_others": paid_for_others,
                "rest_of_group_expenses": rest_of_expenses
            }
        )
