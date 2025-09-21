from sqlalchemy import Column, String, Text, Boolean, DateTime, DECIMAL
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.sql import func
from database import Base
import uuid

# SQLAlchemy model for stores table
class Store(Base):
    __tablename__ = "stores"

    #Primary key
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)

    # Store info
    name = Column(String(255), nullable=False)
    address = Column(Text, nullable=False)
    city = Column(String(100), nullable=False)
    state = Column(String(50), nullable=False)
    zip_code = Column(String(20), nullable=False)
    phone = Column(String(20))
    email =  Column(String(255))

    latitude = Column(DECIMAL(10,8), nullable=False)
    longitude = Column(DECIMAL(11, 8), nullable=False)

    store_type = Column(String(100))

    #user assignmnet
    assigned_user_id = Column(UUID(as_uuid=True), nullable=False)

    #statuses and timestamps
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    def __repr__(self):
        return f"<Store(id={self.id}, name='{self.name}', city='{self.city}')>"

    def to_dict(self):
        return {
            "id": str(self.id),
            "name": self.name,
            "address": self.address,
            "city": self.city,
            "state": self.state,
            "zip_code": self.zip_code,
            "phone": self.phone,
            "email": self.email,
            "latitude": float(self.latitude) if self.latitude else None,
            "longitude": float(self.longitude) if self.longitude else None,
            "store_type": self.store_type,
            "assigned_user_id": str(self.assigned_user_id),
            "is_active": self.is_active,
            "created_at": self.created_at.isoformat() if self.created_at else None,
            "updated_at": self.updated_at.isoformat() if self.updated_at else None
        }