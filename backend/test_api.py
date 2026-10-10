"""Integration tests against the configured real PostgreSQL database."""

from fastapi.testclient import TestClient
from backend.main import app

client = TestClient(app)
DATA = {
    "name": "Integration Test",
    "address_line1": "100 Test Street",
    "address_line2": "",
    "city": "Houston",
    "state": "TX",
    "zip": "77002",
}


def test_crud_and_persistence():
    r = client.post("/api/customers", json=DATA)
    assert r.status_code == 201
    cid = r.json()["customer_id"]
    try:
        assert client.get(f"/api/customers/{cid}").json()["name"] == DATA["name"]
        assert any(c["customer_id"] == cid for c in client.get("/api/customers").json())
        updated = {**DATA, "name": "Updated Test", "zip": "77002-1234"}
        assert client.put(f"/api/customers/{cid}", json=updated).status_code == 200
        from backend.db import connect

        with connect() as conn:
            assert (
                conn.execute(
                    "SELECT name FROM customer WHERE customer_id=%s", (cid,)
                ).fetchone()["name"]
                == "Updated Test"
            )
    finally:
        assert client.delete(f"/api/customers/{cid}").status_code == 204
    assert client.get(f"/api/customers/{cid}").status_code == 404
    assert client.delete(f"/api/customers/{cid}").status_code == 404
    assert client.put(f"/api/customers/{cid}", json=DATA).status_code == 404


def test_invalid_data():
    for field, value in [
        ("name", "  "),
        ("address_line1", ""),
        ("city", ""),
        ("state", "Texas"),
        ("zip", "abc"),
        ("zip", "123456"),
    ]:
        assert (
            client.post("/api/customers", json={**DATA, field: value}).status_code
            == 422
        )


def test_health():
    r = client.get("/api/health")
    assert r.status_code == 200 and r.json()["database"] == "PostgreSQL"
