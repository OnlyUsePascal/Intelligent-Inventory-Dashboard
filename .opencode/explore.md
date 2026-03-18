# Project Exploration Summary

## Overview
**Project:** Intelligent Inventory Dashboard for Keyloop  
**Purpose:** Backend RESTful service simulating Keyloop's Supply product domain - providing dealerships with real-time, centralized visibility over their vehicle inventory pipeline to accelerate "stock turn" and minimize holding risks.

---

## Current Project State

### Structure
```
inventory/
├── src/main/java/com/keyloop/inventory/
│   └── InventoryApplication.java    # Boilerplate Spring Boot main class only
├── src/main/resources/
│   ├── application.yml              # Basic datasource config
│   ├── static/                      # Empty
│   └── templates/                   # Empty
├── src/test/java/com/keyloop/inventory/
│   └── InventoryApplicationTests.java  # Default test class
├── pom.xml                          # Maven config
├── compose.yml                      # Docker Compose (PostgreSQL + app)
├── Dockerfile                       # Multi-stage build
└── Makefile                         # Build automation
```

### What's Already Configured

#### pom.xml Dependencies
| Dependency | Version | Notes |
|------------|---------|-------|
| Spring Boot Parent | 3.5.11 | ✅ Correct |
| spring-boot-starter-data-jpa | managed | ✅ Present |
| spring-boot-starter-web | managed | ✅ Present |
| springdoc-openapi-starter-webmvc-ui | 2.8.16 | ✅ Swagger UI |
| postgresql | managed | ✅ Runtime |
| lombok | managed | ✅ Optional |
| spring-boot-starter-test | managed | ✅ Test scope |

#### compose.yml
- **inventory**: App container on port 8080, depends on PostgreSQL
- **inventory-pg**: PostgreSQL 17, exposed on 5432
- **Network**: `keyloop_net` bridge network

#### Dockerfile Issues Found
```dockerfile
# Line 40 - Bug: incorrect path reference
COPY --from=builder /app/${MY_MODULE}_service/target/*.jar app.jar
# Should be:
COPY --from=builder /app/target/*.jar app.jar
```
Also missing `src` directory structure in COPY command (line 21 copies `src` but should be `src src`).

#### application.yml
- JPA: `ddl-auto: none` (correct for Flyway)
- Hibernate SQL logging enabled
- PostgreSQL dialect configured
- Server port: 8081 (note: compose.yml maps 8080, mismatch)

---

## Gap Analysis: Requirements vs Current State

### Missing Dependencies (pom.xml)
| Required | Purpose | Status |
|----------|---------|--------|
| `spring-boot-starter-validation` | Jakarta validation (@NotNull, @NotBlank) | ❌ Missing |
| `spring-boot-starter-actuator` | Health checks, metrics endpoints | ❌ Missing |
| `micrometer-registry-prometheus` | Prometheus metrics export | ❌ Missing |
| `flyway-core` | Database migrations | ❌ Missing |
| `flyway-database-postgresql` | PostgreSQL Flyway support | ❌ Missing |
| `org.testcontainers:postgresql` | Integration testing | ❌ Missing |
| `org.testcontainers:junit-jupiter` | Testcontainers JUnit 5 | ❌ Missing |

### Missing Application Code
| Component | Status |
|-----------|--------|
| **Entities** | None - need Vehicle, Reservation, VehicleAction, etc. |
| **Repositories** | None |
| **Services** | None |
| **Controllers** | None |
| **DTOs** | None |
| **Security Filter** | None - need header-based tenant/employee extraction |
| **Exception Handlers** | None - need RFC 7807 Problem Details |
| **Config Classes** | None |

### Missing Infrastructure
| Component | Status |
|-----------|--------|
| Flyway migrations | ❌ No `db/migration` folder |
| Prometheus in compose | ❌ Not configured |
| Grafana in compose | ❌ Not configured |
| Structured JSON logging | ❌ Not configured |
| Database seeding (50+ vehicles) | ❌ Not present |

---

## Functional Requirements Breakdown

### FR1: User & Access Management
- Multi-tenancy via `X-Tenant-Id` header
- Employee context via `X-Employee-Id`, `X-Employee-Role` headers
- `OncePerRequestFilter` to populate SecurityContext
- Role-based access: ADMIN, INVENTORY, SALE

### FR2: Vehicle Management
- Full CRUD on vehicles
- Fields: Status, Type, VIN, License Plate, Make, Model, Mileage, etc.
- Filtering by: make, model, status, inventoryType
- Search by: VIN, licensePlate

### FR3: Inventory Tracking & Aging Stock
- Track dates: orderDate, receivedDate, availableForSaleDate
- Dynamic age calculation from receivedDate
- Flag vehicles >90 days as aging stock (`isAgingStock: true`, `daysInInventory: N`)

### FR4: Reservation Management
- SALE/ADMIN can reserve AVAILABLE vehicles
- Track: reservationDate, reservedUntilDate, employeeId
- Auto-update vehicle status: AVAILABLE → RESERVED

### FR5: Actionable Insights
- Log actions/remarks against vehicles
- Track: actionText, timestamp, employeeId
- Retrieve chronological action history per vehicle

---

## Non-Functional Requirements Summary

| NFR | Requirement |
|-----|-------------|
| **NFR1** | Full containerization, single `docker-compose up` |
| **NFR2** | Prometheus metrics at `/metrics`, structured JSON logs with trace IDs |
| **NFR3** | Unit tests (aging stock logic), Integration tests (Testcontainers) |
| **NFR4** | OpenAPI 3.0 spec, Swagger UI, standardized response wrapper, RFC 7807 errors |
| **NFR5** | Pagination (default 20, max 100), indexes on tenant_id, status, make, model |
| **NFR6** | Flyway seeding: 50+ vehicles, 10+ with receivedDate >90 days ago |

---

## Suggested Implementation Phases

### Phase 1: Foundation
- [ ] Fix Dockerfile
- [ ] Add missing Maven dependencies
- [ ] Configure structured logging
- [ ] Setup Flyway with initial schema migration
- [ ] Create base entity classes with auditing

### Phase 2: Core Domain
- [ ] Vehicle entity + repository + service + controller
- [ ] Tenant entity (if needed) or tenant isolation logic
- [ ] Standard response wrapper DTO
- [ ] Global exception handler (RFC 7807)
- [ ] Request validation

### Phase 3: Security & Multi-tenancy
- [ ] Security filter for header extraction
- [ ] SecurityContext population
- [ ] Role-based method security
- [ ] Tenant data isolation in queries

### Phase 4: Business Features
- [ ] Aging stock calculation logic
- [ ] Reservation management
- [ ] Vehicle actions/insights logging

### Phase 5: Observability & Testing
- [ ] Actuator + Prometheus metrics
- [ ] Grafana dashboard in compose
- [ ] Unit tests for aging stock
- [ ] Integration tests with Testcontainers

### Phase 6: Seeding & Polish
- [ ] Flyway migration for 50+ seed vehicles
- [ ] Final Swagger documentation
- [ ] README updates

---

## Design Decisions

| Decision | Resolution |
|----------|------------|
| **Port mismatch** | application.yml says 8081, compose.yml maps 8080 - need to align |
| **Tenant modeling** | ✅ Dedicated `Tenant` entity with `name` column (simple approach) |
| **Employee modeling** | ✅ Store in DB - `Employee` entity needed for reservation references |
| **Vehicle status enum** | ✅ `AVAILABLE`, `RESERVED`, `SOLD`, `UNAVAILABLE` |
| **Inventory type enum** | ✅ `NEW`, `USED`, `DEMO` |

---

## Entity Model Design

Based on the requirements and decisions above:

### Tenant
```
tenant
├── id (UUID, PK)
├── name (VARCHAR, NOT NULL)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)
```

### Employee
```
employee
├── id (UUID, PK)
├── tenant_id (UUID, FK → tenant)
├── name (VARCHAR, NOT NULL)
├── role (ENUM: ADMIN, INVENTORY, SALE)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)
```

### Vehicle
```
vehicle
├── id (UUID, PK)
├── tenant_id (UUID, FK → tenant)
├── vin (VARCHAR, UNIQUE within tenant)
├── license_plate (VARCHAR)
├── make (VARCHAR, NOT NULL)
├── model (VARCHAR, NOT NULL)
├── year (INTEGER)
├── mileage (INTEGER)
├── status (ENUM: AVAILABLE, RESERVED, SOLD, UNAVAILABLE)
├── inventory_type (ENUM: NEW, USED, DEMO)
├── received_date (DATE)
├── available_for_sale_date (DATE)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

Indexes: tenant_id, status, make, model, (tenant_id, vin)
```

### Reservation
```
reservation
├── id (UUID, PK)
├── vehicle_id (UUID, FK → vehicle)
├── employee_id (UUID, FK → employee)
├── reservation_date (TIMESTAMP, NOT NULL)
├── reserved_until_date (TIMESTAMP, NOT NULL)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)
```

### VehicleAction (Insights)
```
vehicle_action
├── id (UUID, PK)
├── vehicle_id (UUID, FK → vehicle)
├── employee_id (UUID, FK → employee)
├── action_text (TEXT, NOT NULL)
├── timestamp (TIMESTAMP, NOT NULL)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)
```

---

## API Endpoints Design

**Global Prefix:** `/api/v1/inventory-vehicles`

### Vehicles (`/api/v1/inventory-vehicles`)
| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/api/v1/inventory-vehicles` | List vehicles (paginated, filterable) | ALL |
| GET | `/api/v1/inventory-vehicles/{id}` | Get vehicle by ID | ALL |
| GET | `/api/v1/inventory-vehicles/search?vin=X` | Search by VIN | ALL |
| GET | `/api/v1/inventory-vehicles/search?licensePlate=X` | Search by license plate | ALL |
| POST | `/api/v1/inventory-vehicles` | Create vehicle | ADMIN, INVENTORY |
| PUT | `/api/v1/inventory-vehicles/{id}` | Update vehicle | ADMIN, INVENTORY |
| DELETE | `/api/v1/inventory-vehicles/{id}` | Delete vehicle | ADMIN |

**Query params for filtering:** `make`, `model`, `status`, `inventoryType`, `page`, `size`, `sort`

### Reservations (`/api/v1/inventory-vehicles/{vehicleId}/reservations`)
| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v1/inventory-vehicles/{vehicleId}/reservations` | Reserve a vehicle | ADMIN, SALE |
| GET | `/api/v1/inventory-vehicles/{vehicleId}/reservations` | Get reservations for vehicle | ALL |
| DELETE | `/api/v1/inventory-vehicles/reservations/{id}` | Cancel reservation | ADMIN, SALE |

### Actions (`/api/v1/inventory-vehicles/{vehicleId}/actions`)
| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v1/inventory-vehicles/{vehicleId}/actions` | Log action against vehicle | ALL |
| GET | `/api/v1/inventory-vehicles/{vehicleId}/actions` | Get action history (chronological) | ALL |

---

## Response Format Design

### Success Response Wrapper
```json
{
  "data": { ... },
  "meta": {
    "timestamp": "2026-03-18T10:30:00Z"
  }
}
```

### Paginated Response
```json
{
  "data": [ ... ],
  "meta": {
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "timestamp": "2026-03-18T10:30:00Z"
  }
}
```

> **Note:** Spring's built-in `Page<T>` object returns a different structure (with `content`, `pageable`, `sort`, etc.). 
> We will need a custom wrapper or DTO mapper to transform Spring's pagination response to match this design.

### Error Response (RFC 7807)
```json
{
  "type": "https://api.keyloop.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/v1/vehicles",
  "errors": [
    { "field": "vin", "message": "must not be blank" }
  ]
}
```

### Vehicle Response (with aging stock fields)
```json
{
  "data": {
    "id": "uuid",
    "vin": "1HGBH41JXMN109186",
    "licensePlate": "ABC123",
    "make": "Honda",
    "model": "Accord",
    "year": 2024,
    "mileage": 15000,
    "status": "AVAILABLE",
    "inventoryType": "USED",
    "receivedDate": "2025-10-15",
    "availableForSaleDate": "2025-10-20",
    "isAgingStock": true,
    "daysInInventory": 154,
    "createdAt": "2025-10-15T08:00:00Z",
    "updatedAt": "2025-10-20T10:00:00Z"
  }
}
```
