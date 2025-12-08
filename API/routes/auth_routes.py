from api.utils.helpers.jwt_utils import JwtUtils
from dependencies.di import get_user_service
from fastapi import APIRouter, Depends, HTTPException, Request, Response
from schemas.user import UserCreate, UserLogin, UserPasswordReset
from services.user_service import IUserService
from utils.helpers import logger
from utils.helpers.constants import ACCESS_TOKEN_FIELD

router = APIRouter(prefix="/auth", tags=["Auth"])

def get_current_user_id(request: Request) -> int:
    """
    Returns the authenticated user id.
    """
    return JwtUtils.auth_wrapper(request)

@router.post("/register")
def register(user_in: UserCreate, user_service: IUserService = Depends(get_user_service)):
    """
    Registers a new user.

    Args:
        user_in (UserCreate) user registration data
        db (Session) database session

    Returns:
        dict created user data and access token

    Exceptions:
        HTTPException returned when registration fails
    """
    try:
        result = user_service.register_user(user_in)
        return result
    except HTTPException as e:
        logger.Logger().error(e)
        raise e
    except ValueError as e:
        logger.Logger().error(e)
        raise HTTPException(status_code=400, detail=str(e))
    
@router.post("/login")
def login(user_in: UserLogin, response: Response, user_service: IUserService = Depends(get_user_service)):
    """
    Logs in a user.

    Args:
        user_in (UserLogin) login credentials
        response (Response) response object for setting cookies
        db (Session) database session

    Returns:
        dict user data and access token

    Exceptions:
        HTTPException returned when authentication fails
    """
    
    try:
        result = user_service.login_user(user_in)
        response.set_cookie(
            key=ACCESS_TOKEN_FIELD,
            value=result.data[ACCESS_TOKEN_FIELD],
            httponly=True,
            secure=True,
            samesite="Lax",
            max_age=60 * 60 * 72 # 3 days
        )

        return result
    except HTTPException as e:
        raise e
    except ValueError as e:
        raise HTTPException(status_code=401, detail=str(e))

@router.get("/me")
def get_currently_logged_in_user(user_id: int = Depends(get_current_user_id), user_service: IUserService = Depends(get_user_service)):
    """
    Returns currently authenticated user.
    """
    return user_service.get_by_id(user_id)

@router.post("/logout")
def logout(response: Response):
    """
    Logs out a user.

    Args:
        response (Response) response used to clear cookies

    Returns:
        dict logout confirmation

    Exceptions:
        None
    """
    
    response.delete_cookie("access_token")
    return {"message": "Logged out successfully."}


@router.post("/password-reset/request")
def request_password_reset(request: Request, user_service = Depends(get_user_service)):
    """
    Requests a password reset link.

    Args:
        request (Request) incoming request with user context
        db (Session) database session

    Returns:
        dict reset token delivery status

    Exceptions:
        HTTPException returned when user lookup fails
    """
    
    #user_id = user_service.auth_wrapper(request)
    #user = user_service.get_user_by_id(user_id)
    #return user_service.request_password_reset(user.email)

@router.post("/password-reset/confirm")
def confirm_password_reset(request: UserPasswordReset, user_service = Depends(get_user_service)):
    """
    Confirms a password reset.

    Args:
        request (UserPasswordReset) reset token and new password
        db (Session) database session

    Returns:
        dict password reset confirmation

    Exceptions:
        HTTPException returned when token or data is invalid
    """

    try:
        user_service.reset_password(request.token, request.new_password)
        return {"message": "Password has been reset successfully."}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
