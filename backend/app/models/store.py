from sqlalchemy import Column, Integer, Text, String, DateTime
from sqlalchemy.orm import relationship
from ..db import Base

class Store(Base):
    __tablename__ = "stores"

    id = Column(String, primary_key=True)
    name = Column(String)
    address = Column(String)
    visits = relationship("Visit", back_populates="store")
