package com.keyloop.inventory.infrastructure.security;

import com.keyloop.inventory.domain.model.EmployeeRole;

import java.util.UUID;

/**
 * ThreadLocal holder for tenant/employee context.
 * Stores authentication information extracted from HTTP headers for the current request.
 */
public final class TenantContext {

    private static final ThreadLocal<TenantContextData> CONTEXT = new ThreadLocal<>();

    private TenantContext() {
        // Utility class
    }

    /**
     * Set the tenant context for the current thread.
     */
    public static void set(UUID tenantId, UUID employeeId, EmployeeRole role) {
        CONTEXT.set(new TenantContextData(tenantId, employeeId, role));
    }

    /**
     * Get the current tenant ID.
     * @return the tenant ID or null if not set
     */
    public static UUID getTenantId() {
        TenantContextData data = CONTEXT.get();
        return data != null ? data.tenantId() : null;
    }

    /**
     * Get the current employee ID.
     * @return the employee ID or null if not set
     */
    public static UUID getEmployeeId() {
        TenantContextData data = CONTEXT.get();
        return data != null ? data.employeeId() : null;
    }

    /**
     * Get the current employee role.
     * @return the role or null if not set
     */
    public static EmployeeRole getRole() {
        TenantContextData data = CONTEXT.get();
        return data != null ? data.role() : null;
    }

    /**
     * Check if the current user has the specified role.
     */
    public static boolean hasRole(EmployeeRole... roles) {
        EmployeeRole currentRole = getRole();
        if (currentRole == null) {
            return false;
        }
        for (EmployeeRole role : roles) {
            if (currentRole == role) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if context is set (user is authenticated).
     */
    public static boolean isAuthenticated() {
        return CONTEXT.get() != null;
    }

    /**
     * Clear the context. MUST be called after request processing.
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * Internal record to hold context data.
     */
    private record TenantContextData(UUID tenantId, UUID employeeId, EmployeeRole role) {
    }
}
