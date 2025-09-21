from fastapi import FastAPI
from .routes.auth import router as auth_router
from .routes.stores import router as stores_router

app = FastAPI(title="FieldSync API", version="0.1.0")

@app.get("/health")
def health():
    return {"status": "ok", "service": "fieldsync-api", "version": "0.1.0"}

app.include_router(auth_router)
#
app.include_router(stores_router)