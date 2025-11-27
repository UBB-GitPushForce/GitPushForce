
from dependencies.di import get_group_log_service
from fastapi import APIRouter, Depends
from services.group_log_service import IGroupLogService
from utils.helpers.jwt_utils import JwtUtils

router = APIRouter(tags=["Group Logs"])

@router.get("/{group_id}")
def get_group_logs(
    group_id: int,
    user_id: int = Depends(JwtUtils.auth_wrapper),
    logs_service: IGroupLogService = Depends(get_group_log_service),
):
    return logs_service.get_logs_for_group(user_id, group_id)
