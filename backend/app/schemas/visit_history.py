from pydantic import BaseModel
from datetime import datetime

from uuid import UUID

class VisitOut(BaseModel):
    visit_id: UUID
    store_id: UUID
    store_name: str
    address: str
    check_in_time: datetime
    check_out_time: datetime | None
    duration_minutes: int | None
    visit_purpose: str
    compliance_status: str
    notes: str | None
