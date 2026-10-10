# Customer Trial Project

INEW 2332 — Sample Trial Project. A working three-layer customer CRUD application using the Python option in the course's **AI Prompt** lesson.

**Repository:** https://github.com/Ayobamicode/customer-trial-project

## Architecture

```text
Angular interface (127.0.0.1:4200)
        │ HTTP /api via development proxy
        ▼
FastAPI REST service (127.0.0.1:8000)
        │ psycopg + parameterized SQL
        ▼
PostgreSQL (127.0.0.1:55432 / customerdb)
        └── CUSTOMER table
```

- **Presentation:** `frontend/src/` — standalone Angular component, customer list, search, validated create/edit form, delete confirmation, and request feedback.
- **Middle layer:** `backend/main.py` — REST routes, Pydantic validation, HTTP status handling, and parameterized SQL. `backend/db.py` manages database connections.
- **Data layer:** `database/schema.sql` and `database/seed.sql` — one CUSTOMER table with generated ID, name, address line 1, address line 2, city, state, and ZIP.

All seeded names and addresses are fictional demonstration data.

## Requirements

- Node.js 24.15+ (or a compatible Angular 22 runtime) and npm.
- Python 3.11+.
- PostgreSQL: use the supplied local npm runner, or an existing PostgreSQL server.

The root install supplies a project-local Node 24.15 runtime so the included npm scripts also work when the system Node is slightly older. PostgreSQL runs locally with generated credentials stored only in ignored `.runtime/` files. No global database installation is required.

## Install

From the project root:

```sh
npm ci
npm ci --prefix frontend
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -r backend/requirements-lock.txt
```

On Windows, activate with `.venv\Scripts\activate` and use `python` instead of `python3` when appropriate. The embedded database package supports common macOS, Windows, and Linux architectures; alternatively use your own PostgreSQL instance.

## Run: three terminals

**Terminal 1 — PostgreSQL:**

```sh
npm run database
```

Wait for “PostgreSQL ready”. Keep this terminal running; Ctrl+C shuts down cleanly and preserves data.

**Terminal 2 — API (from the root, with the virtual environment active):**

```sh
python -m backend.init_db
python -m uvicorn backend.main:app --host 127.0.0.1 --port 8000
```

Initialization creates the table if absent and inserts sample data only when the table is empty. Do not rerun initialization when you intentionally want an empty table.

**Terminal 3 — Angular:**

```sh
npm run frontend
```

Open **http://127.0.0.1:4200**. API documentation is at **http://127.0.0.1:8000/docs**.

### Existing PostgreSQL instead

Create an empty database, set `DATABASE_URL` in the API terminal to its PostgreSQL connection URI, and run the initialization and API commands above. When set, `DATABASE_URL` takes precedence over the local runner's ignored connection file. Do not commit database credentials.

## API

| Method | Endpoint | Behavior |
|---|---|---|
| GET | `/api/health` | Verify real PostgreSQL connectivity |
| GET | `/api/customers` | List customers |
| GET | `/api/customers/{id}` | Read one customer; 404 if absent |
| POST | `/api/customers` | Create; return 201 and Location header |
| PUT | `/api/customers/{id}` | Update; return 200 or 404 |
| DELETE | `/api/customers/{id}` | Delete; return 204 or 404 |

Example request body:

```json
{"name":"Alex Morgan","address_line1":"1200 Example Avenue","address_line2":"Suite 200","city":"Houston","state":"TX","zip":"77002"}
```

Empty required fields, overlong values, malformed state codes, and invalid ZIP codes are rejected. ZIP is text so leading zeroes are preserved. State is a two-letter code; ZIP accepts five digits or ZIP+4. The API returns 422 for validation errors and 503 if PostgreSQL is unavailable.

## Verification

Keep PostgreSQL running, activate the Python environment, and run:

```sh
python -m pytest backend/test_api.py -v
python -m scripts.inspect_database
npm run build
```

Integration tests use the actual configured PostgreSQL database. They create a temporary test record, read it, update it, verify the saved value using SQL, delete it, check 404 behavior, and reject invalid inputs. They preserve the sample records.

## Screenshots

The `screenshots/` folder contains evidence for the interface, REST API, and PostgreSQL layers. See `SUBMISSION.txt` for the GitHub URL and screenshot index.

## Scope

This is a local instructional model. It has no authentication and is bound to loopback for demonstration. A production deployment would require authentication, authorization, migrations, HTTPS, and a production server configuration.

### Recorded validation limits

The production Angular build and three PostgreSQL integration tests passed on
October 9, 2026. Live HTTP health, customer-list, and OpenAPI requests returned 200.
Automated browser preview was blocked by an unavailable browser security-policy
check, so the included screenshots show source code and real recorded API/database
test output. Browser interaction and visual layout have not been verified.
