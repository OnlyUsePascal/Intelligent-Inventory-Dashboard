# Project: Intelligent Inventory Dashboard

## Introduction
### About Keyloop
Keyloop is a leading global provider of automotive technology solutions, acting as the digital backbone for the automotive retail industry. Operating in over 90 countries, Keyloop bridges the gap between dealers, OEMs, technology suppliers, and car buyers to create a seamless, "Experience-First" digital automotive journey.

### About This Project
This project is a backend RESTful service designed to simulate Keyloop's **Supply** product domain. In the automotive retail sector, inventory sitting on a lot depreciates daily. The Supply domain is engineered to combat disconnected "data silos" by providing dealerships with real-time, centralized visibility over their pipeline. The primary goal is to accelerate **"stock turn"** (the speed at which a vehicle is sold after intake) and minimize holding risks.

The Intelligent Inventory Dashboard provides the core APIs necessary to achieve this, enabling managers to track vehicle lifecycles, dynamically identify "aging stock" (>90 days), manage reservations, and log actionable, data-driven insights against specific assets.

---

## Tech Stack
- **Language / Framework:** Java 21 + Spring Boot 3.5.x
- **Key Spring Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`
- **Database:** PostgreSQL 17, `flyway-core` (or Liquibase)
- **Observability:** `micrometer-registry-prometheus`
- **Build Tool:** Maven
- **Testing:** JUnit 5 + Testcontainers

---

## 1. Functional Requirements (FR)

### FR1: User & Access Management
- **FR1.1:** The system must support multi-tenancy, isolating data so that users can only view vehicles and actions associated with their specific tenant (dealership).
- **FR1.2:** Do not implement full JWT/OAuth. Rely on HTTP Headers to mock the security context. Every incoming request will include `X-Tenant-Id`, `X-Employee-Id`, and `X-Employee-Role`. Implement a `OncePerRequestFilter` or `HandlerInterceptor` to extract these and populate the Spring Security Context.
- **FR1.3:** Enforce role-based permissions:
  - **ADMIN:** Full CRUD access.
  - **INVENTORY:** Can manage vehicle data, update dates, and log actions.
  - **SALE:** Can view available inventory, create reservations, and log sales actions.

### FR2: Vehicle Management & Data Modeling
- **FR 2.1:** The system must allow users to create, read, update, and delete vehicle records containing Status, Type, Identification (VIN, License Plate), Description, and Mileage.
- **FR 2.2:** The system must allow users to filter the inventory list by specific attributes, including `make`, `model`, `status`, and `inventoryType`.
- **FR 2.3:** The system must allow users to search for a specific vehicle using its `VIN` or `licensePlate`.

### FR3: Inventory Tracking & Aging Stock Identification
- **FR3.1:** The system must track key inventory lifecycle dates (`orderDate`, `receivedDate`, `availableForSaleDate`).
- **FR3.2:** When retrieving vehicles, dynamically calculate the vehicle's age in inventory by comparing the current system date to the `receivedDate`.
- **FR3.3:** If the calculated age exceeds 90 days, append a boolean flag (`"isAgingStock": true`) and an integer value (`"daysInInventory": 94`) to the API response payload.

### FR4: Reservation Management
- **FR4.1:** Allow a user (`SALE` or `ADMIN` role) to reserve an `AVAILABLE` vehicle.
- **FR4.2:** Log the `reservationDate`, `reservedUntilDate`, and the `employee_id` making the reservation.
- **FR4.3:** Reserving a vehicle automatically updates its overarching status from `AVAILABLE` to `RESERVED`.

### FR5: Actionable Insights
- **FR5.1:** Allow users to log a proposed action/remark (e.g., "Price Reduction Planned") against a specific vehicle.
- **FR5.2:** Each logged action must persistently record the action text, the `timestamp`, and the `employeeId`.
- **FR5.3:** Provide an endpoint to retrieve the chronological history of all actions logged against a specific vehicle.

---

## 2. Non-Functional Requirements (NFR)

### NFR1: Portability & Deployment
- **Requirement:** The application, database, and all accompanying infrastructure must be fully containerized.
- **Requirement:** The entire system must be orchestratable locally using a single `docker-compose up` command.

### NFR2: Observability
- **Requirement (Metrics):** Expose application/runtime metrics via a `/metrics` endpoint, scraped by Prometheus and visualized in a local Grafana dashboard.
- **Requirement (Logs):** Output structured JSON logs to `stdout` including contextual trace IDs.

### NFR3: Quality, Reliability & Validation
- **Requirement:** Core business logic (dynamic calculation of the >90 days aging stock flag) must be covered by Unit Tests.
- **Requirement:** Repository layer and API endpoints must be covered by Integration Tests using Testcontainers.
- **Requirement:** All incoming POST/PUT payloads must be strictly validated using `jakarta.validation` (`@NotNull`, `@NotBlank`, etc.). Validation errors must return a `400 Bad Request`.

### NFR4: API Contract & Documentation
- **Requirement:** Define the API using OpenAPI 3.0 (Swagger) specification, accessible via Swagger UI.
- **Requirement:** All REST API responses must follow a standardized wrapper format. Successful responses wrap in a `data` object. Error responses must adhere to the RFC 7807 Problem Details standard.

### NFR5: Performance & Scalability
- **Requirement:** All list endpoints must implement pagination. Default page size is `20`, max is `100`. Default sorting for the inventory list should be by `receivedDate` descending (oldest stock first).
- **Requirement:** Ensure appropriate SQL indexes on heavily filtered columns (`tenant_id`, `status`, `make`, `model`).

### NFR6: Database Seeding
- **Requirement:** Include a database migration/initialization script (Flyway/Liquibase) that populates the DB on startup.
- **Requirement:** Seed at least 50 vehicles with mixed statuses. At least 10 vehicles MUST have a `receivedDate` older than 90 days from the current system time to demonstrate the aging stock logic.