from fastapi import APIRouter, HTTPException
from ..schemas.user import UserCreate, UserLogin, UserOut, Token
from ..services.auth import create_user, authenticate_user

router = APIRouter(prefix="/auth", tags=["auth"])

@router.post("/register", response_model=UserOut)
def register(payload: UserCreate):
    try:
        return create_user(payload)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@router.post("/login", response_model=Token)
def login(payload: UserLogin):
    token = authenticate_user(payload.email, payload.password)
    if not token:
        raise HTTPException(status_code=401, detail="Invalid credentials")
    return Token(access_token=token)
