from backend.db import connect, ROOT

if __name__ == "__main__":
    with connect() as conn:
        conn.execute((ROOT / "database/schema.sql").read_text())
        conn.execute((ROOT / "database/seed.sql").read_text())
        print(
            "Database initialized:",
            conn.execute("SELECT count(*) AS count FROM customer").fetchone(),
        )
