# Energy Management System

Distributed Systems – Assignment 1 (request-reply communication) and Project P1 (Docker deployment + reverse proxy).

Authenticated users access, monitor and manage smart energy metering devices. The system is a set of loosely coupled,
containerized microservices behind a reverse proxy / API gateway.

## Architecture

UML deployment diagram: [`docs/deployment-diagram.puml`](docs/deployment-diagram.puml)
(render with PlantUML, e.g. the IntelliJ or VS Code plugin, or <https://www.plantuml.com/plantuml>).

| Component | Technology | Responsibility |
|---|---|---|
| **Reverse proxy / API gateway** | Traefik v3.6 + `forwardAuth` middleware | Only published entry point (port 80). Routes requests and asks the Authorization Service to validate the token and the permissions of every protected request before forwarding it. |
| **Frontend** | React 18 (Vite) served by nginx | Login / register, role-based pages. Admin: CRUD on users and devices, assign devices to users. Client: list of own devices. |
| **Authorization Service** (`auth-service`) | Spring Boot, JWT (HS256), PostgreSQL | Login, register, token generation, token + permission validation for the gateway. Credential Database. |
| **User Management** (`user-service`) | Spring Boot, JPA, PostgreSQL | CRUD on users. User Database. Keeps the credentials in sync with the Authorization Service. |
| **Device Management** (`device-service`) | Spring Boot, JPA, PostgreSQL | CRUD on devices, device ↔ user mapping. Device Database. |

Each service has its own database on its own Docker network; the databases are not reachable from anywhere else.

### Request flow

1. The browser sends `POST /api/auth/login` (public route) and receives a JWT plus `userId`, `username` and `role`.
2. For every other API call the frontend sends `Authorization: Bearer <token>`.
3. Traefik calls `GET /api/auth/validate` on the Authorization Service (forwardAuth) with the original method and URI
   (`X-Forwarded-Method`, `X-Forwarded-Uri`). The service checks the signature and expiry of the token, that the user
   still exists, and applies the access rules below.
   - `401` – token missing, invalid or expired; `403` – role not allowed. Traefik returns that response to the browser.
   - `200` – Traefik forwards the request and adds `X-User-Id`, `X-User-Name`, `X-User-Role`.
4. `/api/auth/validate` and `/internal/**` have **no** route in Traefik, so they can only be called from inside the
   Docker network.

### Access rules

| Role | Allowed |
|---|---|
| `ADMIN` | everything under `/api/users/**` and `/api/devices/**` (CRUD on users and devices, assign / unassign) |
| `CLIENT` | `GET /api/users/{ownId}` and `GET /api/devices/user/{ownId}` only |
| none | `POST /api/auth/login`, `POST /api/auth/register`, Swagger UIs |

Self-registration always creates a `CLIENT`. The first administrator is created at startup from `ADMIN_USERNAME` /
`ADMIN_PASSWORD` (see below).

### Keeping users and credentials consistent

The User service owns the user data; the Authorization Service owns the credentials (BCrypt hashes). When a user is
created, updated or deleted, the User service calls `PUT/DELETE /internal/credentials/{userId}` on the Authorization
Service. If that call fails, the operation fails with `503` (a user that was just created is rolled back). Because the
gateway also checks that the user still exists in the Credential Database, deleting a user invalidates their token
immediately.

## Prerequisites

- Docker with Docker Compose v2 (the only mandatory tool)
- For local development without Docker: JDK 25, Maven 3.9+, Node 22

## Build and run

```bash
cp .env.example .env        # then edit: passwords, JWT_SECRET (>= 32 chars), ADMIN_PASSWORD
docker compose up --build
```

| What | URL |
|---|---|
| Web application | <http://localhost/> |
| Traefik dashboard | <http://localhost:8090/> |
| Swagger – auth | <http://localhost/api/auth/swagger-ui.html> |
| Swagger – users | <http://localhost/api/users/swagger-ui.html> |
| Swagger – devices | <http://localhost/api/devices/swagger-ui.html> |

Sign in as `ADMIN_USERNAME` / `ADMIN_PASSWORD` from `.env`. The administrator is created a few seconds after startup,
once the Authorization Service is ready (the User service retries until then).

To call protected endpoints from Swagger UI: log in with `POST /api/auth/login`, copy the `token`, click **Authorize**
and paste it.

Stop with `docker compose down` (add `-v` to delete the database volumes).

If port 80 or 8090 is taken, change `GATEWAY_PORT` / `TRAEFIK_DASHBOARD_PORT` in `.env`.

### Configuration (`.env`)

| Variable | Description |
|---|---|
| `GATEWAY_PORT`, `TRAEFIK_DASHBOARD_PORT` | published ports of the proxy |
| `AUTH_DB_*`, `USER_DB_*`, `DEVICE_DB_*` | database name, user and password of each service |
| `JWT_SECRET` | HS256 signing key, at least 32 characters |
| `JWT_EXPIRATION_MINUTES` | token lifetime (default 60) |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | bootstrap administrator (password at least 6 characters) |

`.env` is git-ignored; never commit real secrets.

### Frontend development

```bash
docker compose up -d --build        # backend through the proxy on :80
cd frontend && npm install && npm run dev    # http://localhost:5173, /api is proxied to the gateway
```

## REST API (summary – full documentation in Swagger)

| Method | Path | Description | Role |
|---|---|---|---|
| POST | `/api/auth/login` | log in, returns JWT | public |
| POST | `/api/auth/register` | create a client account | public |
| GET | `/api/users` | list users | ADMIN |
| GET | `/api/users/{id}` | get a user | ADMIN, owner |
| POST | `/api/users` | create a user | ADMIN |
| PUT | `/api/users/{id}` | update a user (password optional) | ADMIN |
| DELETE | `/api/users/{id}` | delete a user, unassigns their devices | ADMIN |
| GET | `/api/devices` | list devices | ADMIN |
| GET | `/api/devices/{id}` | get a device | ADMIN |
| GET | `/api/devices/user/{userId}` | devices of a user | ADMIN, owner |
| POST | `/api/devices` | create a device | ADMIN |
| PUT | `/api/devices/{id}` | update a device | ADMIN |
| DELETE | `/api/devices/{id}` | delete a device | ADMIN |
| PUT | `/api/devices/{id}/user` | assign a device to a user `{ "userId": "..." }` | ADMIN |
| DELETE | `/api/devices/{id}/user` | remove the assignment | ADMIN |

Errors have the form `{ timestamp, status, error, message, details }` (`400` validation, `401`, `403`, `404`,
`409` duplicate username, `503` another service is down).

Data model: **user** – id (UUID), username, password (BCrypt), name, email, role; **device** – id (UUID), name,
description, maximum consumption (kWh), owner user id (nullable); **credential** – user id, username, password hash, role.

## Testing

With the stack running, `ADMIN_PASSWORD=<value from .env> ./docs/smoke-test.sh` checks the whole flow through the
proxy: 401 without token, login, admin CRUD and assignment, client restrictions (200 for own devices, 403 otherwise),
and that a deleted user's token stops working.

## Database dumps

`docs/auth_db_dump.sql`, `docs/user_db_dump.sql`, `docs/device_db_dump.sql`. Regenerate them from the running
containers with `./docs/export-db-dumps.sh`.

## Repository layout

```
auth-service/      Authorization Service (login, register, JWT, gateway validation)
user-service/      User Management microservice
device-service/    Device Management microservice
frontend/          React web client + nginx
docs/              deployment diagram, DB dumps, smoke test
docker-compose.yml Traefik + services + databases
.env.example       configuration template
```
