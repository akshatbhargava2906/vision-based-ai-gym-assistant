from contextlib import asynccontextmanager

from fastapi import FastAPI

from app import models  # noqa: F401 — ensure models are registered with Base
from app.database import Base, engine
from app.routers.auth import router as auth_router


@asynccontextmanager
async def lifespan(_app: FastAPI):
    # Convenient for the first local milestone. Replace with Alembic migrations
    # before evolving or deploying the schema.
    Base.metadata.create_all(bind=engine)
    yield


app = FastAPI(
    title="Personal Gym Coach API",
    version="0.1.0",
    description="Backend foundation for an Android-first post-set coaching app.",
    lifespan=lifespan,
)
app.include_router(auth_router)


@app.get("/health", tags=["operations"])
def health():
    return {"status": "ok"}
