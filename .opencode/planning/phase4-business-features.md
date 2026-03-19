# Phase 4: Business Features - Execution Plan

## Status: COMPLETE

## Overview
Implement core business logic: aging stock calculation, reservation management, and vehicle actions/insights.

---

## 1. Aging Stock Calculation (FR3)

### Requirements
- **FR3.1:** Track key inventory lifecycle dates (`receivedDate`, `availableForSaleDate`)
- **FR3.2:** Dynamically calculate vehicle age from `receivedDate` to current date
- **FR3.3:** If age >90 days, append `isAgingStock: true` and `daysInInventory: N` to response

### Current State
- `AgingStockCalculator` domain service exists in `domain/service/`
- `VehicleResponse` DTO has `isAgingStock` and `daysInInventory` fields

### Tasks
- [x] Verify `AgingStockCalculator` logic is correct
- [x] Verify `VehicleMapper` applies aging stock calculation when mapping to response
- [ ] Write unit test for aging stock logic (>90 days threshold) - deferred to Phase 5

---

## 2. Reservation Management (FR4)

### Requirements
- **FR4.1:** SALE or ADMIN can reserve an `AVAILABLE` vehicle
- **FR4.2:** Log `reservationDate`, `reservedUntilDate`, and `employeeId`
- **FR4.3:** Reserving auto-updates vehicle status from `AVAILABLE` to `RESERVED`

### Business Rules
- Only `AVAILABLE` vehicles can be reserved
- Creating reservation → vehicle status = `RESERVED`
- Canceling reservation → vehicle status = `AVAILABLE` (if no other active reservations)
- Employee ID comes from `TenantContext`
- **History**: Reservations are soft-deleted (status changed to CANCELLED/EXPIRED)
- **Permissions**: Only creator or ADMIN can cancel a reservation

### Tasks
- [x] Implement `ReservationService.createReservation()`:
  - Validate vehicle exists and belongs to tenant
  - Validate vehicle status is `AVAILABLE`
  - Create reservation with current employee (status = ACTIVE)
  - Update vehicle status to `RESERVED`
  - Return reservation response
- [x] Implement `ReservationService.cancelReservation()`:
  - Validate reservation exists
  - Check permissions (creator or ADMIN only)
  - Update reservation status to `CANCELLED` (soft delete)
  - Update vehicle status to `AVAILABLE` if no other active reservations
- [x] Implement `ReservationService.getReservationsForVehicle()`:
  - Return all reservations for a vehicle (sorted by date desc)
- [x] Handle `VehicleNotAvailableException` properly
- [x] Add `ReservationStatus` enum (ACTIVE, CANCELLED, EXPIRED)
- [x] Add status field to domain model, JPA entity, and DTOs
- [x] Create Flyway migration V2 for reservation status column

---

## 3. Vehicle Actions/Insights (FR5)

### Requirements
- **FR5.1:** Log proposed action/remark against a vehicle
- **FR5.2:** Record `actionText`, `timestamp`, and `employeeId`
- **FR5.3:** Retrieve chronological history of actions for a vehicle

### Tasks
- [x] Implement `VehicleActionService.createAction()`:
  - Validate vehicle exists and belongs to tenant
  - Create action with current employee and timestamp
  - Return action response
- [x] Implement `VehicleActionService.getActionsForVehicle()`:
  - Return all actions for a vehicle
  - Sort by timestamp ascending (chronological)

---

## 4. Scheduled Expiry Job

### Requirements
- Background job runs daily at midnight
- Expires reservations where `reservedUntilDate` has passed
- Updates vehicle status to AVAILABLE if no remaining active reservations

### Tasks
- [x] Create `SchedulingConfig` to enable @Scheduled
- [x] Create `ReservationExpiryService` with daily cron job
- [x] Add repository methods for bulk expiry operations

---

## Decisions

1. **Reservation expiry**: Should we auto-expire reservations when `reservedUntilDate` passes?
   - Decision: **Yes, background job runs daily** to expire reservations

2. **Cancel reservation permissions**: Can only the employee who made the reservation cancel it? Or any ADMIN/SALE?
   - Decision: **Only the creator can cancel** (ADMIN can override)

3. **Multiple reservations**: Can a vehicle have multiple reservation records (history), or just one active at a time?
   - Decision: **Keep history** - vehicles can have multiple reservation records (status tracks state)

4. **Action types**: Should actions have a type/category (e.g., "PRICE_REDUCTION", "INSPECTION", "NOTE"), or just free-text?
   - Decision: **Free-text only** for simplicity

---

## Files Created/Modified

### New Files
- `src/main/java/com/keyloop/inventory/domain/model/ReservationStatus.java`
- `src/main/java/com/keyloop/inventory/config/SchedulingConfig.java`
- `src/main/java/com/keyloop/inventory/application/usecase/ReservationExpiryService.java`
- `src/main/resources/db/migration/V2__add_reservation_status.sql`

### Modified Files
- `src/main/java/com/keyloop/inventory/domain/model/Reservation.java` - added status field
- `src/main/java/com/keyloop/inventory/infrastructure/persistence/entity/ReservationJpaEntity.java` - added status field
- `src/main/java/com/keyloop/inventory/infrastructure/persistence/mapper/ReservationPersistenceMapper.java` - map status
- `src/main/java/com/keyloop/inventory/application/mapper/ReservationMapper.java` - map status
- `src/main/java/com/keyloop/inventory/application/dto/response/ReservationResponse.java` - added status field
- `src/main/java/com/keyloop/inventory/domain/repository/ReservationRepository.java` - added expiry methods
- `src/main/java/com/keyloop/inventory/infrastructure/persistence/repository/SpringDataReservationRepository.java` - updated queries
- `src/main/java/com/keyloop/inventory/infrastructure/persistence/repository/JpaReservationRepository.java` - implemented expiry methods
- `src/main/java/com/keyloop/inventory/application/usecase/ReservationService.java` - permission checks
- `src/main/java/com/keyloop/inventory/infrastructure/web/controller/VehicleController.java` - pass employeeId to cancel

---

## Verification

- [x] Compile: `./mvnw compile`
- [ ] Run unit tests for aging stock (Phase 5)
- [ ] Manual API test: create reservation, verify vehicle status changes (Phase 5)
- [ ] Manual API test: cancel reservation, verify vehicle status reverts (Phase 5)
- [ ] Manual API test: log action, retrieve history (Phase 5)
