from typing import Optional, Dict
from ..schemas.user import UserCreate, UserOut
from ..core.security import hash_password, verify_password, create_access_token

# In-memory user "db" for initial wiring (replace with real DB later)
_users_by_email: Dict[str, dict] = {}
_id_counter = 1

def create_user(data: UserCreate) -> UserOut:
    global _id_counter
    if data.email in _users_by_email:
        raise ValueError("Email already registered")
    record = {
        "id": _id_counter,
        "email": data.email,
        "full_name": data.full_name,
        "hashed_password": hash_password(data.password),
    }
    _users_by_email[data.email] = record
    _id_counter += 1
    return UserOut(id=record["id"], email=record["email"], full_name=record["full_name"])

def authenticate_user(email: str, password: str) -> Optional[str]:
    user = _users_by_email.get(email)
    if not user:
        return None
    if not verify_password(password, user["hashed_password"]):
        return None
    return create_access_token(subject=str(user["id"]))
