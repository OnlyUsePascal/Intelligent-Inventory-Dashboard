# Inventory Service

Spring Boot service for managing dealership inventory, reservations, and vehicle actions. It exposes REST APIs with Swagger UI and emits metrics for Prometheus/Grafana.

Regarding the design document, please see this [link]()

## Stack
- Java 21, Spring Boot 3.5
- PostgreSQL, Flyway
- SpringDoc OpenAPI, Actuator, Micrometer
- Docker Compose for local infra

## Setup

### Environment Variables

For the scope of this assessment, the `.env` file is already present in this repo.

- `POSTGRES_URI` (required): JDBC URL for PostgreSQL.

- `SEED_DATA` (optional, default false): when true, seed data is inserted at startup.

### Infrastructure

```sh
docker compose up --detach inventory-pg prometheus grafana

# or Makefile for short
make infra
```

### App

We can either use the containerized version, or directly build the app

#### Docker

```sh
docker compose up --detach inventory

# or Makefile for short
make app.dock
```

#### Bare metal

```sh
./mvnw clean package -DskipTests

java -jar target/inventory-0.0.1-SNAPSHOT.jar

# or Makefile for short
make app.run
```

#### Test

```bash
./mvnw test

# or Makefile for short
make app.test
```

## Seeding
Set `SEED_DATA=true` before startup. On launch, the app clears existing data and writes `seed-output.txt` with tenant/employee IDs and ready-to-use `X-User-Context` headers.

Example snippet from `seed-output.txt`:
```text
TENANTS
tenant.id=... tenant.name=...

READY_TO_USE_HEADERS
X-User-Context: tenant|employee|role
```

## Useful Links (after app starts)
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Actuator (open by default): http://localhost:8080/actuator
- Prometheus UI: http://localhost:9090
- Grafana UI: http://localhost:3000 (default user/pass: admin/admin)

## AI Usage
- Research company product context and domain expectations
- Expand and define functional and non-functional requirements
- Orchestrate development into multiple phases, planning, implementation, testing, and code review
