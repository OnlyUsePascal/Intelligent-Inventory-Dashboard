# Phase 6: Seeding & Swagger - Execution Plan

## Overview
Add Java-based data seeding (no Flyway) and finalize Swagger/OpenAPI docs.

---

## 1. Data Seeding (Java)

### Requirements
- Seeder runs only when `SEED_DATA=true` is present in `.env`
- Seeder wipes all data before seeding
- Seed **20 vehicles** with mixed distribution
- Include some aging stock, reservations, and actions
- Write seed output to `seed-output.txt` with tenant, employee info, and ready-to-use `X-User-Context` headers

### Tasks
- [ ] Add seeder component that runs on startup when `SEED_DATA=true`
- [ ] Purge tables in safe order: `vehicle_action` → `reservation` → `vehicle` → `employee` → `tenant`
- [ ] Seed base tenant(s) and employee(s)
- [ ] Seed 20 vehicles with mixed status/inventory type
- [ ] Add a few reservations and vehicle actions
- [ ] Ensure clear logs when seeding runs/skips

---

## 2. Swagger/OpenAPI Finalization

### Goals
- Ensure all endpoints are fully documented
- Add request/response examples
- Document standard error responses
- Show required headers globally

### Tasks
- [ ] Audit controller annotations for summaries, parameters, and responses
- [ ] Add example payloads for create vehicle, reserve vehicle, log action
- [ ] Ensure 400/403/404/409 responses appear in docs
- [ ] Verify header params (tenant, employee, role) are visible in Swagger UI

---

## Verification

- [ ] Run app with `SEED_DATA=true` and confirm data is reseeded
- [ ] Run app without `SEED_DATA` and confirm no seeding occurs
- [ ] Review Swagger UI for completeness
