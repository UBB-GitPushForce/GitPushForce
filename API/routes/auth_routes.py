from database import get_db
from fastapi import APIRouter, Depends, HTTPException, Request, Response
from schemas.user import UserCreate, UserLogin, UserPasswordReset
from services.user_service import UserService
from sqlalchemy.orm import Session

router = APIRouter(prefix="/auth", tags=["Auth"])
@router.get("/")
def get_all_users(db: Session = Depends(get_db)):
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
    Register a new user and return a JWT token.
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
        raise e
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    
@router.post("/login")
def login(user_in: UserLogin, response: Response, db: Session = Depends(get_db)):
    """
    Login an existing user and set the JWT token in an HTTP-only cookie.
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
    Logs out the user by deleting the JWT cookie.
    """
    response.delete_cookie("access_token")
    return {"message": "Logged out successfully."}

@router.get("/me")
def get_current_user(request: Request, db: Session = Depends(get_db)):
    """
    Returns the currently authenticated user (decoded from JWT).
    """
    service = UserService(db)
    user_id = service.auth_wrapper(request)
    # Fetch the user from DB
    user = service.get_user_by_id(user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    # Return only relevant fields
    return {
        "id": user.id,
        "email": user.email,
        "first_name": user.first_name,
        "last_name": user.last_name
    }

@router.post("/password-reset/request")
def request_password_reset(request: Request, db: Session = Depends(get_db)):
    """
    Request a password reset link/token to be sent to the user's email.
    """
    service = UserService(db)
    user_id = service.auth_wrapper(request)
    user = service.get_user_by_id(user_id)
    return service.request_password_reset(user.email)

@router.post("/password-reset/confirm")
def confirm_password_reset(request: UserPasswordReset, db: Session = Depends(get_db)):
    """
    Reset the user's password using the token from their email.
    """
    service = UserService(db)

    try:
        service.reset_password(request.token, request.new_password)
        return {"message": "Password has been reset successfully."}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
