from database import get_db
from fastapi import APIRouter, Depends, HTTPException, Request, Response
from schemas.user import UserCreate, UserLogin, UserPasswordReset
from services.user_service import UserService
from sqlalchemy.orm import Session
from utils.helpers import logger

router = APIRouter(prefix="/auth", tags=["Auth"])
@router.get("/")
def get_all_users(db: Session = Depends(get_db)):
    """
    Retrieves all users.

    Args:
        db (Session) database session

    Returns:
        list users returned from the service

    Exceptions:
        HTTPException returned on service error
    """
    service = UserService(db)
    try:
        return service.get_all_users()
    except HTTPException as e:
        raise e
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.post("/register")
def register(user_in: UserCreate, db: Session = Depends(get_db)):
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
    
    service = UserService(db)
    try:
        result = service.register_user(user_in)
        return {
        "message": "User registered successfully.",
        "access_token": result["access_token"],
        "user": result["user"]
        }
    except HTTPException as e:
        logger.Logger().error(e)
        raise e
    except ValueError as e:
        logger.Logger().error(e)
        raise HTTPException(status_code=400, detail=str(e))
    
@router.post("/login")
def login(user_in: UserLogin, response: Response, db: Session = Depends(get_db)):
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
    
    service = UserService(db)
    try:
        result = service.login_user(user_in)
        response.set_cookie(
            key="access_token",
            value=result["access_token"],
            httponly=True,
            secure=True,
            samesite="Lax",
            max_age=60 * 60 * 72 # 3 days
        )

        return {"message": "Login successful", "user": result["user"], "access_token": result["access_token"]}
    except HTTPException as e:
        raise e
    except ValueError as e:
        raise HTTPException(status_code=401, detail=str(e))

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
def request_password_reset(request: Request, db: Session = Depends(get_db)):
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
    
    service = UserService(db)
    user_id = service.auth_wrapper(request)
    user = service.get_user_by_id(user_id)
    return service.request_password_reset(user.email)

@router.post("/password-reset/confirm")
def confirm_password_reset(request: UserPasswordReset, db: Session = Depends(get_db)):
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

    service = UserService(db)

    try:
        service.reset_password(request.token, request.new_password)
        return {"message": "Password has been reset successfully."}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
