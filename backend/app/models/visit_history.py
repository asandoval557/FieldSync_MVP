import uuid
from sqlalchemy import Column, String, Integer, DateTime, ForeignKey, Text, DECIMAL
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship
from datetime import datetime
from ..db import Base

class Visit(Base):
    __tablename__ = "visits"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    store_id = Column(UUID(as_uuid=True), ForeignKey("stores.id", ondelete="CASCADE"), nullable=False)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False)

    check_in_time = Column(DateTime(timezone=True), nullable=False)
    check_out_time = Column(DateTime(timezone=True))
    duration_minutes = Column(Integer)

    check_in_latitude = Column(DECIMAL(10, 8))
    check_in_longitude = Column(DECIMAL(11, 8))
    check_out_latitude = Column(DECIMAL(10, 8))
    check_out_longitude = Column(DECIMAL(11, 8))

    notes = Column(Text)
    visit_purpose = Column(String(100))
    compliance_status = Column(String(50))

    created_at = Column(DateTime(timezone=True), default=datetime.utcnow)
    updated_at = Column(DateTime(timezone=True), default=datetime.utcnow)


    store = relationship("Store", back_populates="visits")
    user = relationship("User", back_populates="visits")
