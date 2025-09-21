from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime
from uuid import UUID

from ..models.visit_history import Visit
from ..models.store import Store
from ..schemas.visit_history import VisitOut
from ..db import get_db


router = APIRouter()

@router.get("/visits", response_model=List[VisitOut])
def get_filtered_visits(
        user_id: UUID,
        start_date: Optional[datetime] = Query(None),
        end_date: Optional[datetime] = Query(None),
        store_id: Optional[UUID] = Query(None),
        db: Session = Depends(get_db)
):
    query = db.query(Visit).filter(Visit.user_id == str(user_id))

    if start_date:
        query = query.filter(Visit.check_in_time >= start_date)
    if end_date:
        query = query.filter(Visit.check_in_time <= end_date)
    if store_id:
        query = query.filter(Visit.store_id == store_id)

    visits = query.all()

    results = []
    for visit in visits:
        duration = visit.duration_minutes or (
            int((visit.check_out_time - visit.check_in_time).total_seconds() / 60)
            if visit.check_out_time else None
        )
        results.append(VisitOut(
            visit_id=visit.id,
            store_id=visit.store_id,
            store_name=visit.store.name,
            address=visit.store.address,
            check_in_time=visit.check_in_time,
            check_out_time=visit.check_out_time,
            duration_minutes=duration,
            visit_purpose=visit.visit_purpose,
            compliance_status=visit.compliance_status,
            notes=visit.notes
        ))

    return results