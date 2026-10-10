from typing import Annotated
from fastapi import FastAPI, HTTPException, Response
from pydantic import BaseModel, ConfigDict, Field, StringConstraints
import psycopg
from backend.db import connect

app = FastAPI(
    title="Customer CRUD API",
    version="1.0.0",
    description="INEW 2332 • Angular → FastAPI → PostgreSQL",
)
State = Annotated[str, StringConstraints(to_upper=True, pattern=r"^[A-Z]{2}$")]
Zip = Annotated[str, StringConstraints(pattern=r"^[0-9]{5}(-[0-9]{4})?$")]


class CustomerInput(BaseModel):
    model_config = ConfigDict(str_strip_whitespace=True, extra="forbid")
    name: str = Field(min_length=1, max_length=100)
    address_line1: str = Field(min_length=1, max_length=150)
    address_line2: str = Field(default="", max_length=150)
    city: str = Field(min_length=1, max_length=80)
    state: State
    zip: Zip


class Customer(CustomerInput):
    customer_id: int


@app.get("/api/health", tags=["System"])
def health():
    with connect() as conn:
        version = conn.execute("SELECT version() AS version").fetchone()["version"]
    return {"status": "ok", "database": "PostgreSQL", "version": version}


@app.get("/api/customers", response_model=list[Customer], tags=["Customers"])
def list_customers():
    with connect() as conn:
        return conn.execute("SELECT * FROM customer ORDER BY customer_id").fetchall()


@app.get("/api/customers/{customer_id}", response_model=Customer, tags=["Customers"])
def get_customer(customer_id: int):
    with connect() as conn:
        row = conn.execute(
            "SELECT * FROM customer WHERE customer_id=%s", (customer_id,)
        ).fetchone()
    if not row:
        raise HTTPException(404, "Customer not found")
    return row


@app.post(
    "/api/customers", response_model=Customer, status_code=201, tags=["Customers"]
)
def create_customer(customer: CustomerInput, response: Response):
    with connect() as conn:
        row = conn.execute(
            "INSERT INTO customer (name,address_line1,address_line2,city,state,zip) VALUES (%s,%s,%s,%s,%s,%s) RETURNING *",
            tuple(customer.model_dump().values()),
        ).fetchone()
    response.headers["Location"] = f"/api/customers/{row['customer_id']}"
    return row


@app.put("/api/customers/{customer_id}", response_model=Customer, tags=["Customers"])
def update_customer(customer_id: int, customer: CustomerInput):
    with connect() as conn:
        row = conn.execute(
            "UPDATE customer SET name=%s,address_line1=%s,address_line2=%s,city=%s,state=%s,zip=%s WHERE customer_id=%s RETURNING *",
            (*customer.model_dump().values(), customer_id),
        ).fetchone()
    if not row:
        raise HTTPException(404, "Customer not found")
    return row


@app.delete("/api/customers/{customer_id}", status_code=204, tags=["Customers"])
def delete_customer(customer_id: int):
    with connect() as conn:
        row = conn.execute(
            "DELETE FROM customer WHERE customer_id=%s RETURNING customer_id",
            (customer_id,),
        ).fetchone()
    if not row:
        raise HTTPException(404, "Customer not found")
    return Response(status_code=204)


@app.exception_handler(psycopg.OperationalError)
async def database_unavailable(request, exc):
    from fastapi.responses import JSONResponse

    return JSONResponse(
        status_code=503,
        content={"detail": "Database unavailable. Check PostgreSQL and DATABASE_URL."},
    )
