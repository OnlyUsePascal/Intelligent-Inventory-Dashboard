# Phase 5: Testing - Execution Plan

## Overview
Add unit and integration tests to cover core business logic and all API endpoints.

---

## 1. Unit Tests

### Coverage Targets
- **Aging stock calculation**: validate `daysInInventory` and `isAgingStock` threshold (>90 days)
- **VehicleService CRUD**: create, update, delete, get, list (tenant isolation enforced)
- **ReservationService**:
  - create reservation for AVAILABLE vehicle
  - reject reservation for non-AVAILABLE vehicle
  - cancel reservation permissions (creator or ADMIN only)
  - status transitions (ACTIVE → CANCELLED)
  - vehicle status updates (RESERVED → AVAILABLE when no active reservations)
- **VehicleActionService**: create action and list ordering
- **Mappers**: VehicleMapper aging fields, ReservationMapper status mapping

### Tasks
- [ ] Create unit test fixtures/builders for Vehicle, Reservation, VehicleAction
- [ ] Add tests for `AgingStockCalculator` and mapper aging flags
- [ ] Add tests for `VehicleService` CRUD + tenant validation
- [ ] Add tests for `ReservationService` conflicts and permissions
- [ ] Add tests for `VehicleActionService` create/list order

---

## Verification

- [ ] Run: `./mvnw test`
