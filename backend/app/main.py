from contextlib import asynccontextmanager
from fastapi import FastAPI
from .routes.auth import router as auth_router
from .routes.note import router as notes_router
from .routes.visit_history import router as visit_history_router
from .db import Base, engine
from .models import user
import uvicorn


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup logic
    Base.metadata.create_all(bind=engine)
    yield

app = FastAPI(title="FieldSync API", version="0.1.0", lifespan=lifespan)

@app.get("/health")
def health():
    return {"status": "ok", "service": "fieldsync-api", "version": "0.1.0"}

app.include_router(auth_router)
app.include_router(notes_router)
app.include_router(visit_history_router)
