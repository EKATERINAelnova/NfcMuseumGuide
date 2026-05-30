from datetime import datetime, timezone
from typing import Optional

from sqlalchemy import Column, JSON, UniqueConstraint
from sqlmodel import Field, SQLModel


def now_utc() -> datetime:
    return datetime.now(timezone.utc)


class ExhibitBase(SQLModel):
    title_ru: str
    title_en: str
    subtitle_ru: str
    subtitle_en: str
    description_ru: str
    description_en: str
    zone: str = "Новый зал"
    floor: int = 1
    century: str = "Новая коллекция"
    category: str = "Коллекция"
    route_order: int = 99
    tags: list[str] = Field(default_factory=list, sa_column=Column(JSON, nullable=False))
    facts: list[str] = Field(default_factory=list, sa_column=Column(JSON, nullable=False))
    image_uri: Optional[str] = None
    nfc_code: Optional[str] = None
    is_custom: bool = True
    is_deleted: bool = False


class Exhibit(ExhibitBase, table=True):
    __tablename__ = "exhibits"

    id: str = Field(primary_key=True, index=True)
    created_at: datetime = Field(default_factory=now_utc, nullable=False)
    updated_at: datetime = Field(default_factory=now_utc, nullable=False)


class ExhibitCreate(ExhibitBase):
    id: str


class ExhibitUpdate(SQLModel):
    title_ru: Optional[str] = None
    title_en: Optional[str] = None
    subtitle_ru: Optional[str] = None
    subtitle_en: Optional[str] = None
    description_ru: Optional[str] = None
    description_en: Optional[str] = None
    zone: Optional[str] = None
    floor: Optional[int] = None
    century: Optional[str] = None
    category: Optional[str] = None
    route_order: Optional[int] = None
    tags: Optional[list[str]] = None
    facts: Optional[list[str]] = None
    image_uri: Optional[str] = None
    nfc_code: Optional[str] = None
    is_custom: Optional[bool] = None
    is_deleted: Optional[bool] = None


class ScanLog(SQLModel, table=True):
    __tablename__ = "scan_logs"

    id: Optional[int] = Field(default=None, primary_key=True)
    timestamp: datetime = Field(default_factory=now_utc, nullable=False, index=True)
    exhibit_id: str = Field(index=True)
    source: str = Field(index=True)
    message: str
    device_id: str = Field(default="android", index=True)


class ScanLogCreate(SQLModel):
    exhibit_id: str
    source: str
    message: str
    device_id: str = "android"
    timestamp_ms: Optional[int] = None


class Favorite(SQLModel, table=True):
    __tablename__ = "favorites"
    __table_args__ = (UniqueConstraint("device_id", "exhibit_id", name="favorite_device_exhibit_uc"),)

    id: Optional[int] = Field(default=None, primary_key=True)
    device_id: str = Field(index=True)
    exhibit_id: str = Field(index=True)
    created_at: datetime = Field(default_factory=now_utc, nullable=False)


class FavoriteCreate(SQLModel):
    device_id: str = "android"
    exhibit_id: str
