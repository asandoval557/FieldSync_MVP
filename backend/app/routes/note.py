from datetime import datetime
from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from ..db import get_db
from ..models.note import Note
from ..schemas.note import NoteCreate, NoteUpdate, NoteOut

router = APIRouter(prefix="/visits", tags=["notes"])

def current_user_id() -> int:
    return 1

@router.get("/{visit_id}/notes", response_model=List[NoteOut])
def list_notes(visit_id: int, db: Session = Depends(get_db)):
    return (
        db.query(Note)
        .filter(Note.visit_id == visit_id)
        .order_by(Note.created_at.desc())
        .all()
    )

@router.post("/{visit_id}/notes", response_model=NoteOut, status_code=status.HTTP_201_CREATED)
def create_note(
        visit_id: int,
        payload: NoteCreate,
        db: Session = Depends(get_db),
        user_id: int = Depends(current_user_id),
):
    note = Note(
        visit_id=visit_id,
        author_id=user_id,
        body=payload.body,
        photo_url=payload.photo_url,
    )
    db.add(note)
    db.commit()
    db.refresh(note)
    return note

@router.put("/{visit_id}/notes/{note_id}", response_model=NoteOut)
def update_note(
        visit_id: int,
        note_id: int,
        payload: NoteUpdate,
        db: Session = Depends(get_db),
):
    note = db.get(Note, note_id)
    if not note or note.visit_id != visit_id:
        raise HTTPException(status_code=404, detail="Note not found")
    if payload.body is not None:
        note.body = payload.body
    if payload.photo_url is not None:
        note.photo_url = payload.photo_url
    note.updated_at = datetime.utcnow()
    db.commit()
    db.refresh(note)
    return note

@router.delete("/{visit_id}/notes/{note_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_note(visit_id: int, note_id: int, db: Session = Depends(get_db)):
    note = db.get(Note, note_id)
    if not note or note.visit_id != visit_id:
        raise HTTPException(status_code=404, detail="Note not found")
    db.delete(note)
    db.commit()
    return None
