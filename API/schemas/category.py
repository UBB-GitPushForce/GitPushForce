from typing import List, Optional

from pydantic import BaseModel, Field


class CategoryBase(BaseModel):
    title: str = Field(..., min_length=1, max_length=30)
    keywords: List[str] = Field(default_factory=list)

    class Config:
        from_attributes = True

class CategoryCreate(CategoryBase):
    pass

class CategoryUpdate(BaseModel):
    title: Optional[str] = Field(None, min_length=1, max_length=30)
    keywords: Optional[List[str]] = Field(default_factory=list)

class CategoryResponse(CategoryBase):
    id: int
    user_id: int