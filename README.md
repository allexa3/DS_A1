# Spring Demo – Distributed Systems Lab

A small REST API for managing persons, built with Spring Boot and MySQL and packaged with Docker.

## Tech stack

| Component    | Version          |
|--------------|------------------|
| Java         | 25 (LTS)         |
| Spring Boot  | 4.1              |
| Hibernate    | 7                |
| MySQL        | 8.4 (LTS)        |
| H2           | in-memory, tests only |
| Maven        | 3.9+             |
| Docker       | Compose v2       |

## Prerequisites

- **JDK 25** – to build and run locally
- **Maven 3.9+** – or use the IDE's bundled Maven
- **Docker** with Docker Compose v2 (`docker compose`, not `docker-compose`)

## Configuration

Credentials are **not** stored in the repository. They are read from a `.env` file in the project root.

1. Copy the example file:
   ```bash
   cp .env.example .env
   ```
2. Edit `.env` and set your own values:
   ```
   # port mapped in docker-compose
   DB_PORT=3306
   DB_NAME=city_db
   DB_USER=city_app
   DB_PASSWORD=change-me
   DB_ROOT_PASSWORD=change-me-too
   ```

> **Note:** `.env` is read both by Docker Compose and by Spring (as a properties file).
> Put comments on their own line and don't quote values.

### Environment variables

| Variable           | Used by        | Description                              | Default (local) |
|--------------------|----------------|------------------------------------------|-----------------|
| `DB_HOST`          | app            | Database host                            | `localhost`     |
| `DB_PORT`          | app            | Database port                            | `3306`          |
| `DB_NAME`          | app, MySQL     | Database name                            | –               |
| `DB_USER`          | app, MySQL     | Application database user                | –               |
| `DB_PASSWORD`      | app, MySQL     | Application database password            | –               |
| `DB_ROOT_PASSWORD` | MySQL          | MySQL root password                      | –               |
| `PORT`             | app            | HTTP port                                | `8080`          |

## Running the application

### Option 1 – everything in Docker

```bash
docker compose up --build
```

This starts:
- **mysql-db** – MySQL 8.4, data kept in the `mysql-data` volume
- **spring-demo** – the application, started once MySQL reports healthy

Stop it with `docker compose down`. Add `-v` to also delete the database volume.

### Option 2 – app in the IDE, database in Docker

Useful for debugging.

```bash
docker compose up -d mysql-db
```

Then run `Ds2020Application` from IntelliJ, or:

```bash
mvn spring-boot:run
```

The app reads `.env` automatically and connects to MySQL on `localhost:3306`.

> If MySQL is also installed directly on your machine, port 3306 is already taken.
> Change the mapping in `docker-compose.yml` to `"3307:3306"` and set `DB_PORT=3307` in `.env`.

## API

Base URL: `http://localhost:8080/spring-demo`

| Method | Endpoint          | Description                  |
|--------|-------------------|------------------------------|
| GET    | `/`               | Status check                 |
| GET    | `/person`         | List all persons             |
| GET    | `/person/{id}`    | Get one person by UUID       |
| POST   | `/person`         | Create a person              |
| GET    | `/actuator/health`| Application health           |

### Example: create a person

```bash
curl -X POST http://localhost:8080/spring-demo/person \
     -H "Content-Type: application/json" \
     -d '{"name": "John", "address": "Somewhere Else street", "age": 22}'
```

Returns `201 Created` with the new person's UUID.

Validation rules:
- `name` and `address` are required
- `age` must be greater than 18

Invalid input returns `400 Bad Request` with details about the failed fields.

## Tests and code quality

```bash
mvn clean verify
```

This runs:
- **Checkstyle** (`checkstyle.xml`) – error-level violations fail the build, warnings are only reported
- **Unit and integration tests** – JUnit 5, using an in-memory H2 database (no MySQL needed)
- **JaCoCo** coverage check – minimum 10% line coverage

Coverage report: `target/site/jacoco/index.html`

Run the tests only:

```bash
mvn test
```

## Project structure

```
src/main/java/ro/tuc/ds2020
├── Ds2020Application.java        # entry point
├── controllers/                  # REST endpoints
│   └── handlers/                 # global exception handling
│       └── exceptions/model/     # custom exceptions + error response DTO
├── dtos/                         # request/response objects
│   ├── builders/                 # entity <-> DTO mapping
│   └── validators/               # custom @AgeLimit validation
├── entities/                     # JPA entities
├── repositories/                 # Spring Data repositories
└── services/                     # business logic

src/main/resources/application.properties   # app configuration
src/test/resources/application-test.properties  # H2 test configuration
src/test/resources/test-sql/                 # test data scripts
```