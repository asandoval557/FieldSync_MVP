from fastapi import FastAPI
from .routes.auth import router as auth_router
from .routes.note import router as notes_router
from .routes.visit_history import router as visit_history_router
from .db import Base, engine


app = FastAPI(title="FieldSync API", version="0.1.0")

@app.on_event("startup")
def on_startup():
    Base.metadata.create_all(bind=engine)

@app.get("/health")
def health():
    return {"status": "ok", "service": "fieldsync-api", "version": "0.1.0"}

app.include_router(auth_router)
app.include_router(notes_router)
app.include_router(visit_history_router)
