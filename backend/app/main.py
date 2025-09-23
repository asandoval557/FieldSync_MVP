from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(title="FieldSync API", version="0.1.0")

# Permissive CORS for dev/demo; tighten later
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- Existing routes ---
from .routes.auth import router as auth_router
app.include_router(auth_router, prefix="/auth", tags=["auth"])

# --- Peer branch: store routes (one of these may exist) ---
stores_router = None
try:
    from .routes.stores import router as stores_router  # e.g., routes/stores.py
except Exception:
    try:
        from .routes.store import router as stores_router   # e.g., routes/store.py
    except Exception:
        stores_router = None

if stores_router is not None:
    app.include_router(stores_router, prefix="/stores", tags=["stores"])

@app.get("/health")
def health():
    return {"status": "ok"}
