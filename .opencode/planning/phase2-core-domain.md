# Phase 2: Core Domain - Execution Plan

## Overview
Build the core domain layer using **Clean Architecture** and **Domain-Driven Design (DDD)** principles.

---

## Architecture Overview

### Clean Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE                            │
│  (Controllers, JPA Repositories, External Services)          │
├─────────────────────────────────────────────────────────────┤
│                    APPLICATION                               │
│  (Use Cases / Application Services, DTOs, Mappers)           │
├─────────────────────────────────────────────────────────────┤
│                      DOMAIN                                  │
│  (Entities, Value Objects, Domain Services, Repository       │
│   Interfaces, Domain Events, Exceptions)                     │
└─────────────────────────────────────────────────────────────┘
```

### Dependency Rule
- Inner layers know nothing about outer layers
- Domain has NO dependencies on Spring/JPA
- Application depends on Domain
- Infrastructure depends on Application and Domain

---

## Package Structure

```
com.keyloop.inventory/
├── domain/                          # DOMAIN LAYER (no Spring dependencies)
│   ├── model/                       # Entities & Value Objects
│   │   ├── Tenant.java
│   │   ├── Employee.java
│   │   ├── Vehicle.java
│   │   ├── Reservation.java
│   │   ├── VehicleAction.java
│   │   ├── VehicleStatus.java       # Enum
│   │   ├── InventoryType.java       # Enum
│   │   └── EmployeeRole.java        # Enum
│   ├── repository/                  # Repository Interfaces (ports)
│   │   ├── TenantRepository.java
│   │   ├── EmployeeRepository.java
│   │   ├── VehicleRepository.java
│   │   ├── ReservationRepository.java
│   │   └── VehicleActionRepository.java
│   ├── service/                     # Domain Services (pure business logic)
│   │   └── AgingStockCalculator.java
│   └── exception/                   # Domain Exceptions
│       ├── EntityNotFoundException.java
│       ├── VehicleNotAvailableException.java
│       └── DomainException.java
│
├── application/                     # APPLICATION LAYER
│   ├── usecase/                     # Use Cases (Application Services)
│   │   ├── vehicle/
│   │   │   ├── CreateVehicleUseCase.java
│   │   │   ├── UpdateVehicleUseCase.java
│   │   │   ├── DeleteVehicleUseCase.java
│   │   │   ├── GetVehicleUseCase.java
│   │   │   └── ListVehiclesUseCase.java
│   │   ├── reservation/
│   │   │   ├── CreateReservationUseCase.java
│   │   │   ├── CancelReservationUseCase.java
│   │   │   └── GetReservationsUseCase.java
│   │   └── action/
│   │       ├── LogVehicleActionUseCase.java
│   │       └── GetVehicleActionsUseCase.java
│   ├── dto/                         # DTOs (request/response)
│   │   ├── request/
│   │   │   ├── CreateVehicleRequest.java
│   │   │   ├── UpdateVehicleRequest.java
│   │   │   ├── CreateReservationRequest.java
│   │   │   └── CreateVehicleActionRequest.java
│   │   └── response/
│   │       ├── VehicleResponse.java
│   │       ├── ReservationResponse.java
│   │       ├── VehicleActionResponse.java
│   │       └── EmployeeResponse.java
│   └── mapper/                      # Entity <-> DTO mappers
│       ├── VehicleMapper.java
│       ├── ReservationMapper.java
│       └── VehicleActionMapper.java
│
├── infrastructure/                  # INFRASTRUCTURE LAYER
│   ├── persistence/                 # JPA implementations
│   │   ├── entity/                  # JPA Entities (DB representation)
│   │   │   ├── TenantJpaEntity.java
│   │   │   ├── EmployeeJpaEntity.java
│   │   │   ├── VehicleJpaEntity.java
│   │   │   ├── ReservationJpaEntity.java
│   │   │   └── VehicleActionJpaEntity.java
│   │   ├── repository/              # JPA Repository implementations
│   │   │   ├── JpaTenantRepository.java
│   │   │   ├── JpaEmployeeRepository.java
│   │   │   ├── JpaVehicleRepository.java
│   │   │   ├── JpaReservationRepository.java
│   │   │   └── JpaVehicleActionRepository.java
│   │   └── mapper/                  # Domain <-> JPA Entity mappers
│   │       ├── TenantPersistenceMapper.java
│   │       ├── EmployeePersistenceMapper.java
│   │       ├── VehiclePersistenceMapper.java
│   │       ├── ReservationPersistenceMapper.java
│   │       └── VehicleActionPersistenceMapper.java
│   └── web/                         # REST Controllers
│       ├── controller/
│       │   └── VehicleController.java
│       ├── dto/                     # API-specific wrappers
│       │   ├── ApiResponse.java
│       │   ├── PagedResponse.java
│       │   └── PageMeta.java
│       └── exception/               # Exception handlers
│           └── GlobalExceptionHandler.java
│
└── config/                          # Spring Configuration
    └── JpaConfig.java
```

---

## DDD Concepts Applied

### Aggregates
- **Vehicle Aggregate**: Vehicle (root), Reservation, VehicleAction
  - Vehicle is the aggregate root
  - Reservations and Actions are accessed through Vehicle

### Entities vs Value Objects
- **Entities** (have identity): Tenant, Employee, Vehicle, Reservation, VehicleAction
- **Value Objects** (immutable, no identity): VehicleStatus, InventoryType, EmployeeRole (enums)

### Repository Pattern
- Domain defines repository **interfaces** (ports)
- Infrastructure provides JPA **implementations** (adapters)

### Domain Services
- `AgingStockCalculator` - calculates if vehicle is aging stock (>90 days)
- Pure business logic, no infrastructure dependencies

---

## Implementation Order

### Step 1: Domain Layer (no Spring dependencies)
- [ ] Create domain model classes (POJOs, no JPA annotations)
- [ ] Create enums (VehicleStatus, InventoryType, EmployeeRole)
- [ ] Create repository interfaces (ports)
- [ ] Create domain exceptions
- [ ] Create AgingStockCalculator domain service

### Step 2: Infrastructure - Persistence Layer
- [ ] Create JPA entities (with @Entity, @Table, etc.)
- [ ] Create Spring Data JPA repositories
- [ ] Create persistence mappers (Domain <-> JPA Entity)
- [ ] Create repository adapter implementations

### Step 3: Application Layer
- [ ] Create request/response DTOs with validation
- [ ] Create application mappers (Domain <-> DTO)
- [ ] Create use cases (application services)

### Step 4: Infrastructure - Web Layer
- [ ] Create API wrapper DTOs (ApiResponse, PagedResponse)
- [ ] Create controllers
- [ ] Create GlobalExceptionHandler (RFC 7807)

### Step 5: Verification
- [ ] Compile and run
- [ ] Test basic CRUD endpoints

---

## Key Design Decisions

| Decision | Approach |
|----------|----------|
| **Domain entities** | Pure POJOs, no JPA annotations |
| **JPA entities** | Separate classes in infrastructure layer |
| **Mapping** | Manual mappers (no MapStruct for simplicity) |
| **Use Cases** | One class per use case for Single Responsibility |
| **Validation** | Jakarta validation on request DTOs (application layer) |
| **Pagination** | Custom PagedResponse wrapper in web layer |

---

## Notes

- **Why separate Domain and JPA entities?** 
  - Domain model stays clean, framework-agnostic
  - Can change persistence without touching domain
  - Easier to test domain logic in isolation

- **Role-based access:** Will be enforced in Phase 3
- **Tenant isolation:** Will be enforced in Phase 3
- **Aging stock:** Calculated in domain service, returned in DTO
