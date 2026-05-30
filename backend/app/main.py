import os
import shutil
from datetime import datetime, timezone
from pathlib import Path
from uuid import uuid4

from fastapi import Depends, FastAPI, File, HTTPException, Request, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from sqlmodel import Session, select

from .database import create_db_and_tables, engine, get_session
from .models import Exhibit, ExhibitCreate, ExhibitUpdate, Favorite, FavoriteCreate, ScanLog, ScanLogCreate
from .seed_data import DEFAULT_EXHIBITS

UPLOAD_DIR = Path("uploads")
UPLOAD_DIR.mkdir(exist_ok=True)

app = FastAPI(
    title="NFC Museum Guide API",
    description="FastAPI + PostgreSQL backend for the Android NFC Museum Guide app.",
    version="1.0.0",
)

origins = [origin.strip() for origin in os.getenv("CORS_ORIGINS", "*").split(",") if origin.strip()]
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
app.mount("/uploads", StaticFiles(directory=str(UPLOAD_DIR)), name="uploads")




def public_base_url(request: Request) -> str:
    """Return a URL reachable by the current client.

    If Android calls the API through http://10.0.2.2:8000 or http://192.168.x.x:8000,
    request.base_url contains exactly that address. This avoids storing localhost URLs
    in PostgreSQL, because localhost on a phone means the phone itself, not the PC.
    """
    configured = os.getenv("APP_BASE_URL", "").strip().rstrip("/")
    if configured and not any(host in configured for host in ("localhost", "127.0.0.1", "0.0.0.0")):
        return configured
    return str(request.base_url).rstrip("/")

def timestamp_ms_to_datetime(timestamp_ms: int | None) -> datetime:
    if timestamp_ms is None:
        return datetime.now(timezone.utc)
    return datetime.fromtimestamp(timestamp_ms / 1000, tz=timezone.utc)


def seed_if_empty(session: Session) -> None:
    exists = session.exec(select(Exhibit).limit(1)).first()
    if exists is not None:
        return
    for item in DEFAULT_EXHIBITS:
        exhibit = Exhibit(**item)
        session.add(exhibit)
    session.commit()


@app.on_event("startup")
def on_startup() -> None:
    create_db_and_tables()
    with Session(engine) as session:
        seed_if_empty(session)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.get("/api/exhibits", response_model=list[Exhibit])
def list_exhibits(include_deleted: bool = False, session: Session = Depends(get_session)) -> list[Exhibit]:
    statement = select(Exhibit).order_by(Exhibit.route_order)
    if not include_deleted:
        statement = statement.where(Exhibit.is_deleted == False)  # noqa: E712
    return list(session.exec(statement).all())


@app.get("/api/exhibits/{exhibit_id}", response_model=Exhibit)
def get_exhibit(exhibit_id: str, session: Session = Depends(get_session)) -> Exhibit:
    exhibit = session.get(Exhibit, exhibit_id)
    if exhibit is None or exhibit.is_deleted:
        raise HTTPException(status_code=404, detail="Exhibit not found")
    return exhibit


@app.post("/api/exhibits", response_model=Exhibit, status_code=201)
def create_exhibit(payload: ExhibitCreate, session: Session = Depends(get_session)) -> Exhibit:
    if session.get(Exhibit, payload.id) is not None:
        raise HTTPException(status_code=409, detail="Exhibit with this id already exists")
    data = payload.dict()
    data["nfc_code"] = data.get("nfc_code") or payload.id
    exhibit = Exhibit(**data, updated_at=datetime.now(timezone.utc))
    session.add(exhibit)
    session.commit()
    session.refresh(exhibit)
    return exhibit


@app.put("/api/exhibits/{exhibit_id}", response_model=Exhibit)
def upsert_exhibit(exhibit_id: str, payload: ExhibitCreate, session: Session = Depends(get_session)) -> Exhibit:
    data = payload.dict()
    data["id"] = exhibit_id
    data["nfc_code"] = data.get("nfc_code") or exhibit_id
    current = session.get(Exhibit, exhibit_id)
    if current is None:
        exhibit = Exhibit(**data, updated_at=datetime.now(timezone.utc))
        session.add(exhibit)
    else:
        for key, value in data.items():
            setattr(current, key, value)
        current.is_deleted = False
        current.updated_at = datetime.now(timezone.utc)
        exhibit = current
    session.commit()
    session.refresh(exhibit)
    return exhibit


@app.patch("/api/exhibits/{exhibit_id}", response_model=Exhibit)
def update_exhibit(exhibit_id: str, payload: ExhibitUpdate, session: Session = Depends(get_session)) -> Exhibit:
    exhibit = session.get(Exhibit, exhibit_id)
    if exhibit is None or exhibit.is_deleted:
        raise HTTPException(status_code=404, detail="Exhibit not found")
    for key, value in payload.dict(exclude_unset=True).items():
        setattr(exhibit, key, value)
    exhibit.updated_at = datetime.now(timezone.utc)
    session.add(exhibit)
    session.commit()
    session.refresh(exhibit)
    return exhibit


@app.delete("/api/exhibits/{exhibit_id}")
def delete_exhibit(exhibit_id: str, session: Session = Depends(get_session)) -> dict[str, bool]:
    exhibit = session.get(Exhibit, exhibit_id)
    if exhibit is None:
        raise HTTPException(status_code=404, detail="Exhibit not found")
    exhibit.is_deleted = True
    exhibit.updated_at = datetime.now(timezone.utc)
    session.add(exhibit)
    session.commit()
    return {"ok": True}


@app.post("/api/exhibits/{exhibit_id}/image", response_model=Exhibit)
def upload_exhibit_image(
    exhibit_id: str,
    request: Request,
    image: UploadFile = File(...),
    session: Session = Depends(get_session),
) -> Exhibit:
    exhibit = session.get(Exhibit, exhibit_id)
    if exhibit is None or exhibit.is_deleted:
        raise HTTPException(status_code=404, detail="Exhibit not found")

    suffix = Path(image.filename or "image.jpg").suffix.lower() or ".jpg"
    filename = f"{exhibit_id}-{uuid4().hex}{suffix}"
    destination = UPLOAD_DIR / filename
    with destination.open("wb") as buffer:
        shutil.copyfileobj(image.file, buffer)

    exhibit.image_uri = f"{public_base_url(request)}/uploads/{filename}"
    exhibit.updated_at = datetime.now(timezone.utc)
    session.add(exhibit)
    session.commit()
    session.refresh(exhibit)
    return exhibit


@app.post("/api/scan-logs", response_model=ScanLog, status_code=201)
def create_scan_log(payload: ScanLogCreate, session: Session = Depends(get_session)) -> ScanLog:
    log = ScanLog(
        timestamp=timestamp_ms_to_datetime(payload.timestamp_ms),
        exhibit_id=payload.exhibit_id,
        source=payload.source,
        message=payload.message,
        device_id=payload.device_id,
    )
    session.add(log)
    session.commit()
    session.refresh(log)
    return log


@app.get("/api/scan-logs", response_model=list[ScanLog])
def list_scan_logs(
    device_id: str | None = None,
    limit: int = 120,
    session: Session = Depends(get_session),
) -> list[ScanLog]:
    statement = select(ScanLog).order_by(ScanLog.timestamp.desc()).limit(limit)
    if device_id:
        statement = statement.where(ScanLog.device_id == device_id)
    return list(session.exec(statement).all())


@app.get("/api/stats/scan-counts")
def scan_counts(session: Session = Depends(get_session)) -> dict[str, int]:
    logs = session.exec(select(ScanLog)).all()
    counts: dict[str, int] = {}
    for log in logs:
        counts[log.exhibit_id] = counts.get(log.exhibit_id, 0) + 1
    return counts


@app.post("/api/favorites", response_model=Favorite, status_code=201)
def add_favorite(payload: FavoriteCreate, session: Session = Depends(get_session)) -> Favorite:
    existing = session.exec(
        select(Favorite).where(
            Favorite.device_id == payload.device_id,
            Favorite.exhibit_id == payload.exhibit_id,
        )
    ).first()
    if existing is not None:
        return existing
    favorite = Favorite(device_id=payload.device_id, exhibit_id=payload.exhibit_id)
    session.add(favorite)
    session.commit()
    session.refresh(favorite)
    return favorite


@app.delete("/api/favorites/{device_id}/{exhibit_id}")
def remove_favorite(device_id: str, exhibit_id: str, session: Session = Depends(get_session)) -> dict[str, bool]:
    favorite = session.exec(
        select(Favorite).where(Favorite.device_id == device_id, Favorite.exhibit_id == exhibit_id)
    ).first()
    if favorite is not None:
        session.delete(favorite)
        session.commit()
    return {"ok": True}


@app.get("/api/favorites/{device_id}", response_model=list[Favorite])
def list_favorites(device_id: str, session: Session = Depends(get_session)) -> list[Favorite]:
    return list(session.exec(select(Favorite).where(Favorite.device_id == device_id)).all())
