from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
import sys
import os

#Fast API routes for store management endpoints

sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from database import get_db
from models.store import Store

router = APIRouter(prefix="/stores", tags=["stores"])

@router.get("/{user_id}")
async def get_user_stores(user_id: str, db: Session = Depends(get_db)):
    try:
        # Query stores for the specified user
        stores = db.query(Store).filter(Store.assigned_user_id == user_id).all()

        if not stores:
            raise HTTPException(status_code=404, detail="No stores found for this user")

        # Convert stores to dictionaries for JSON response
        store_list = [store.to_dict() for store in stores]

        return {
            "user_id": user_id,
            "store_count": len(store_list),
            "stores": store_list
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

@router.get("/")
async def get_all_stores(db: Session = Depends(get_db)):
    try:
        stores = db.query(Store).all()
        store_list = [store.to_dict() for store in stores]

        return {
            "total_stores": len(store_list),
            "stores": store_list
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")