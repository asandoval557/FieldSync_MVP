from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field

class NoteCreate(BaseModel):
    body: str = Field(min_length=1, max_length=5000)
    photo_url: Optional[str] = None

class NoteUpdate(BaseModel):
    body: Optional[str] = Field(default=None, min_length=1, max_length=5000)
    photo_url: Optional[str] = None

class NoteOut(BaseModel):
    id: int
    visit_id: int
    author_id: Optional[int] = None
    body: str
    photo_url: Optional[str] = None
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True
