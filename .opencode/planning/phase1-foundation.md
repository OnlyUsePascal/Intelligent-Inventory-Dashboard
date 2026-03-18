# Phase 1: Foundation - Execution Plan

## 1. Fix Dockerfile
- Fix line 21: `COPY src .` → `COPY src src`
- Fix line 40: Remove `${MY_MODULE}_service/` from the path → `COPY --from=builder /app/target/*.jar app.jar`

## 2. Add Missing Maven Dependencies to `pom.xml`
```xml
<!-- Validation -->
spring-boot-starter-validation

<!-- Observability -->
spring-boot-starter-actuator
micrometer-registry-prometheus

<!-- Database Migrations -->
flyway-core
flyway-database-postgresql

<!-- Testing (Testcontainers) -->
org.testcontainers:testcontainers (test scope)
org.testcontainers:postgresql (test scope)
org.testcontainers:junit-jupiter (test scope)
```

## 3. Configure Structured JSON Logging
- Add `logstash-logback-encoder` dependency for JSON logging
- Create `src/main/resources/logback-spring.xml` with JSON output format including trace IDs

## 4. Setup Flyway with Initial Schema Migration
- Create `src/main/resources/db/migration/` folder
- Create `V1__initial_schema.sql` with tables:
  - `tenant` (id, name, created_at, updated_at)
  - `employee` (id, tenant_id, name, role, created_at, updated_at)
  - `vehicle` (all columns from explore.md)
  - `reservation` (id, vehicle_id, employee_id, dates, timestamps)
  - `vehicle_action` (id, vehicle_id, employee_id, action_text, timestamp, timestamps)
  - All indexes on tenant_id, status, make, model
- Add Flyway config to `application.yml`

## 5. Fix Port Mismatch
- Update `application.yml` server port from 8081 → 8080 (to match compose.yml)

## 6. Create Base Entity Classes with Auditing
- Create `BaseEntity.java` with:
  - `id` (UUID, auto-generated)
  - `createdAt`, `updatedAt` (with JPA auditing annotations)
- Enable JPA auditing in main application class or config

---

## Decisions

| Question | Decision |
|----------|----------|
| **Seeding data** | Separate Java seeder class (not in Flyway migrations) - keeps schema and data independent |
| **Enum storage** | VARCHAR in PostgreSQL (simpler, easier to modify) |

## Updated Task: Data Seeder
- Create `DataSeeder.java` component that runs on startup (using `ApplicationRunner` or `CommandLineRunner`)
- Conditionally seeds data (e.g., only if DB is empty, or via profile/property flag)
- Will be implemented in Phase 6, not Phase 1
