from datetime import datetime
from sqlalchemy import Column, Integer, Text, String, DateTime
from sqlalchemy.orm import declarative_base
from ..db import Base

class Note(Base):
    __tablename__ = "notes"

    id = Column(Integer, primary_key=True, index=True)
    visit_id = Column(Integer, index=True, nullable=False)
    author_id = Column(Integer, index=True, nullable=True)
    body = Column(Text, nullable=False)
    photo_url = Column(String, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, nullable=False)
