from pydantic import BaseModel, EmailStr

class UserBase(BaseModel):
    email: EmailStr

class UserCreate(UserBase):
    password: str
    full_name: str | None = None

class UserLogin(UserBase):
    password: str

class UserOut(UserBase):
    id: int
    full_name: str | None = None

class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"
