# Phase 3: Security & Multi-tenancy - Execution Plan

## Overview
Implement header-based authentication, role-based access control (RBAC), and tenant data isolation.

---

## Requirements (from FR1)

- **FR1.1:** Multi-tenancy - users can only view data for their tenant (dealership)
- **FR1.2:** HTTP headers for auth context (no JWT/OAuth):
  - `X-Tenant-Id` - tenant UUID
  - `X-Employee-Id` - employee UUID  
  - `X-Employee-Role` - role (ADMIN, INVENTORY, SALE)
- **FR1.3:** Role-based permissions:
  - **ADMIN:** Full CRUD access
  - **INVENTORY:** Manage vehicles, update dates, log actions
  - **SALE:** View inventory, create reservations, log actions

---

## Architecture

### Security Flow
```
Request → TenantContextFilter → Controller → Service → Repository
              ↓
         Extract headers
         Validate tenant/employee exist
         Store in TenantContext (ThreadLocal)
              ↓
         Role checked via @PreAuthorize
              ↓
         Repository queries filtered by tenantId
```

---

## Package Structure

```
com.keyloop.inventory/
├── infrastructure/
│   ├── security/
│   │   ├── TenantContext.java           # ThreadLocal holder for tenant/employee
│   │   ├── TenantContextFilter.java     # OncePerRequestFilter - extract headers
│   │   ├── SecurityConfig.java          # Spring Security config (permit paths, etc.)
│   │   └── RoleConstants.java           # Role name constants
│   └── web/
│       └── controller/                  # Add @PreAuthorize to endpoints
```

---

## Implementation Steps

### Step 1: Create TenantContext (ThreadLocal)
- `TenantContext.java` - holds current tenant ID, employee ID, and role
- Static methods: `setContext()`, `getTenantId()`, `getEmployeeId()`, `getRole()`, `clear()`
- ThreadLocal to ensure request isolation

```java
public class TenantContext {
    private static final ThreadLocal<TenantContextData> context = new ThreadLocal<>();
    
    public static void set(UUID tenantId, UUID employeeId, EmployeeRole role) { ... }
    public static UUID getTenantId() { ... }
    public static UUID getEmployeeId() { ... }
    public static EmployeeRole getRole() { ... }
    public static void clear() { ... }
}
```

### Step 2: Create TenantContextFilter (OncePerRequestFilter)
- Extract `X-Tenant-Id`, `X-Employee-Id`, `X-Employee-Role` from headers
- Validate:
  - All headers present (return 400 if missing)
  - Tenant exists in DB (return 401 if not found)
  - Employee exists and belongs to tenant (return 401 if not found)
  - Employee role matches header (return 403 if mismatch)
- Populate `TenantContext`
- Clear context in `finally` block

### Step 3: Create SecurityConfig
- Configure Spring Security (no full auth, just filter chain)
- Permit actuator, swagger, health endpoints
- Apply filter to API endpoints

### Step 4: Add Role-Based Access to Controller
Using `@PreAuthorize` or manual checks in controller:

| Endpoint | ADMIN | INVENTORY | SALE |
|----------|-------|-----------|------|
| GET /vehicles | ✅ | ✅ | ✅ |
| GET /vehicles/{id} | ✅ | ✅ | ✅ |
| GET /vehicles/search | ✅ | ✅ | ✅ |
| POST /vehicles | ✅ | ✅ | ❌ |
| PUT /vehicles/{id} | ✅ | ✅ | ❌ |
| DELETE /vehicles/{id} | ✅ | ❌ | ❌ |
| POST /reservations | ✅ | ❌ | ✅ |
| DELETE /reservations | ✅ | ❌ | ✅ |
| GET /reservations | ✅ | ✅ | ✅ |
| POST /actions | ✅ | ✅ | ✅ |
| GET /actions | ✅ | ✅ | ✅ |

### Step 5: Add Tenant Isolation to Repositories/Services
- All queries must filter by `tenantId = TenantContext.getTenantId()`
- Update `VehicleService` to inject tenant ID into queries
- Update `VehicleRepository` methods to accept tenant ID parameter

### Step 6: Update Services to Use Employee Context
- `ReservationService.createReservation()` - use `TenantContext.getEmployeeId()`
- `VehicleActionService.logAction()` - use `TenantContext.getEmployeeId()`

---

## Task Checklist

- [ ] Create `TenantContext` (ThreadLocal holder)
- [ ] Create `TenantContextFilter` (OncePerRequestFilter)
- [ ] Create `SecurityConfig` (Spring Security configuration)
- [ ] Create `RoleConstants` (role name constants)
- [ ] Add header validation (missing headers → 400)
- [ ] Add tenant/employee validation (not found → 401)
- [ ] Add role mismatch handling (→ 403)
- [ ] Add `@PreAuthorize` or role checks to controller endpoints
- [ ] Update repository interfaces to include tenant ID in queries
- [ ] Update services to filter by tenant ID
- [ ] Update services to use employee ID from context
- [ ] Add exception handling for security errors in GlobalExceptionHandler
- [ ] Verify with compile
- [ ] Test endpoints with different roles

---

## Error Responses

### Missing Headers (400 Bad Request)
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Missing required header: X-Tenant-Id",
  "instance": "/api/v1/inventory-vehicles"
}
```

### Invalid Tenant/Employee (401 Unauthorized)
```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Invalid tenant or employee",
  "instance": "/api/v1/inventory-vehicles"
}
```

### Insufficient Permissions (403 Forbidden)
```json
{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "Insufficient permissions for this operation",
  "instance": "/api/v1/inventory-vehicles"
}
```

---

## Notes

- **No Spring Security authentication** - we trust the headers (simulating gateway/proxy auth)
- **ThreadLocal cleanup** - critical to clear context after request to prevent leaks
- **Swagger/Actuator** - exclude from filter to allow access without headers
- **Testing** - will need to pass headers in all API tests
