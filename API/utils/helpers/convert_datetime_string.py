from datetime import datetime
from typing import Optional

from fastapi import HTTPException
from utils.helpers.constants import STATUS_BAD_REQUEST


def parse_date_string(date_str: Optional[str]) -> Optional[datetime]:
    """
    Converts a date string into a datetime object.
    """
    if not date_str:
        return None
    
    try:
        return datetime.fromisoformat(date_str.replace('Z', '+00:00'))
    except ValueError:
        try:
            return datetime.strptime(date_str, "%Y-%m-%d")
        except ValueError:
            raise HTTPException(
                status_code=STATUS_BAD_REQUEST,
                detail=f"Invalid date format: {date_str}. Use YYYY-MM-DD or YYYY-MM-DDTHH:MM:SS"
            )