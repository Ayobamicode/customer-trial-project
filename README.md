# Customer Trial Project — Spring Boot

**INEW 2332 Sample Trial Project** • Angular + Java Spring Boot + PostgreSQL

Repository: https://github.com/Ayobamicode/customer-trial-project

A three-layer customer CRUD application implementing the **Java option** in the course AI Prompt lesson. All sample names and addresses are fictional.

## Layers

| Layer | Implementation | Files |
|---|---|---|
| Presentation | Angular 22, forms, search, create/edit/delete feedback | `frontend/src/` |
| REST API | Java 21, Spring Boot 3.5.16, Spring MVC, Bean Validation, Spring JDBC | `backend/src/main/java/edu/hcc/customer/` |
| Data | PostgreSQL 18, a single CUSTOMER table | `database/schema.sql`, `database/seed.sql` |

```text
Angular :4200 → HTTP /api development proxy → Spring Boot :8000
                                                  ↓ Spring JDBC
                                      PostgreSQL :55432 / customerdb
```

`CustomerController` defines REST routes. `CustomerInput` validates and normalizes input. `CustomerRepository` executes prepared SQL with named parameters. `ApiExceptionHandler` returns readable validation and error responses. JSON uses snake_case so the Angular model matches the database columns.

## Prerequisites

- **Java JDK 21** (make sure `java -version` and `JAVA_HOME` select this JDK).
- **Node.js and npm**. The root npm install includes a project-local Node 24.15 runtime compatible with Angular 22.
- Internet access on first install for npm and Maven dependencies.

The included Maven wrapper downloads Maven 3.9.11; a global Maven installation is unnecessary. The npm database runner starts real PostgreSQL locally, with no Docker requirement.

## Install

From the project root:

```sh
npm ci
npm ci --prefix frontend
```

## Run (three terminals)

**1. Database — project root:**

```sh
npm run database
```

Wait for “PostgreSQL ready.” This creates the schema and seeds the table only when empty. It generates a local password in ignored `.runtime/` files and writes Spring's local connection configuration. Keep the terminal running. Ctrl+C shuts PostgreSQL down cleanly; records persist on disk. Restarting an intentionally emptied table inserts the three demo rows again.

**2. Spring Boot — backend folder:**

```sh
cd backend
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd spring-boot:run`. Spring loads `../.runtime/application-local.properties`. No Python environment or Python server is required.

**3. Angular — project root:**

```sh
npm run frontend
```

Open **http://127.0.0.1:4200**. API reference: **http://127.0.0.1:8000/docs/index.html**. Health: **http://127.0.0.1:8000/api/health**.

## Use an existing PostgreSQL server

Create an empty database, run `database/schema.sql` and `database/seed.sql` with psql or your database client, then set the Spring environment variables before launching the API:

- `SPRING_DATASOURCE_URL` — JDBC URL, e.g. `jdbc:postgresql://127.0.0.1:5432/customerdb`
- `SPRING_DATASOURCE_USERNAME` — your database user
- `SPRING_DATASOURCE_PASSWORD` — your database password

These standard Spring environment properties override the local connection file. Do not commit credentials. The optional `npm run inspect-db` helper is for the included local PostgreSQL runner.

## REST API

| Method | Path | Result |
|---|---|---|
| GET | `/api/health` | 200, Spring Boot and real PostgreSQL version |
| GET | `/api/customers` | 200, all customers |
| GET | `/api/customers/{id}` | 200 or 404 |
| POST | `/api/customers` | 201, saved customer and Location header |
| PUT | `/api/customers/{id}` | 200 or 404 |
| DELETE | `/api/customers/{id}` | 204 or 404 |

POST/PUT body:

```json
{"name":"Alex Morgan","address_line1":"1200 Example Avenue","address_line2":"Suite 200","city":"Houston","state":"TX","zip":"77002"}
```

Required strings are trimmed and cannot be blank. State is normalized to two uppercase letters. ZIP is text (preserving leading zeros), accepting five digits or ZIP+4. Address line 2 is optional. Invalid input returns **400** with a `detail` message. Missing records return **404**; lost database connectivity returns **503**.

## Test and package

Keep PostgreSQL running. In `backend`:

```sh
./mvnw clean verify
```

`CustomerApiTest` launches Spring Boot on a random port and sends real HTTP requests backed by PostgreSQL. It tests CRUD, persistence with a separate SQL query, missing records, invalid input, state normalization, leading-zero ZIP preservation, and database health. It removes its temporary customer and preserves the sample rows.

The packaged executable is `backend/target/customer-api-1.0.0.jar`. From the root:

```sh
java -jar backend/target/customer-api-1.0.0.jar
npm run build
npm run inspect-db
```

## Evidence

The `screenshots/` directory and `SUBMISSION.txt` document the final Spring Boot implementation and validation. `backend/test-results.txt` records JUnit results; `backend/verification.txt` and `database/verification.txt` contain real API and database output.

## Scope

A local instructional model, bound to loopback, without authentication. Production deployment requires authentication, authorization, HTTPS, migrations, and appropriate hosting configuration.
