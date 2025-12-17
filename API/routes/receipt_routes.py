from dependencies.di import get_receipt_service
from fastapi import APIRouter, Depends, File, Request, UploadFile
from services.receipt_service import IReceiptService
from utils.helpers.jwt_utils import JwtUtils

router = APIRouter(prefix="/receipt", tags=["Receipt"])

def get_current_user_id(request: Request) -> int:
    """
    Returns the authenticated user id.
    """
    return JwtUtils.auth_wrapper(request)

@router.post("/process-receipt")
def process_receipt(image: UploadFile = File(...), user_id: int = Depends(get_current_user_id), receipt_service: IReceiptService = Depends(get_receipt_service)):
    return receipt_service.process_receipt_photo(image, user_id)
