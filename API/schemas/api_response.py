from typing import Any, Optional

from pydantic import BaseModel


class APIResponse(BaseModel):
    success: bool = True
    message: Optional[str] = None
    data: Optional[Any] = None