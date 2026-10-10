import os
from pathlib import Path
import psycopg
from psycopg.rows import dict_row

ROOT = Path(__file__).resolve().parents[1]


def connect():
    url = os.getenv("DATABASE_URL")
    if not url:
        url_file = ROOT / ".runtime/database-url"
        if not url_file.exists():
            raise RuntimeError("Start PostgreSQL first, or set DATABASE_URL.")
        url = url_file.read_text().strip()
    return psycopg.connect(url, row_factory=dict_row, connect_timeout=5)
