package com.keyloop.inventory.infrastructure.security;

import com.keyloop.inventory.domain.model.EmployeeRole;
import com.keyloop.inventory.infrastructure.security.exception.ForbiddenException;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utility class for role-based access control checks.
 * Use this in controllers to validate that the current user has the required role.
 */
public final class RoleGuard {

    private RoleGuard() {
        // Utility class
    }

    /**
     * Require that the current user has one of the specified roles.
     * @param roles the required roles (any one will satisfy the requirement)
     * @throws ForbiddenException if the user doesn't have any of the required roles
     */
    public static void requireRole(EmployeeRole... roles) {
        if (!TenantContext.hasRole(roles)) {
            String roleNames = Arrays.stream(roles)
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw ForbiddenException.requiresRole(roleNames);
        }
    }

    /**
     * Require that the current user is an ADMIN.
     * @throws ForbiddenException if the user is not an ADMIN
     */
    public static void requireAdmin() {
        requireRole(EmployeeRole.ADMIN);
    }

    /**
     * Require that the current user is an ADMIN or INVENTORY manager.
     * @throws ForbiddenException if the user doesn't have the required role
     */
    public static void requireAdminOrInventory() {
        requireRole(EmployeeRole.ADMIN, EmployeeRole.INVENTORY);
    }

    /**
     * Require that the current user is an ADMIN or SALE.
     * @throws ForbiddenException if the user doesn't have the required role
     */
    public static void requireAdminOrSale() {
        requireRole(EmployeeRole.ADMIN, EmployeeRole.SALE);
    }

    /**
     * Check if the current user has one of the specified roles (without throwing).
     * @param roles the roles to check
     * @return true if the user has one of the roles
     */
    public static boolean hasRole(EmployeeRole... roles) {
        return TenantContext.hasRole(roles);
    }

    /**
     * Check if the current user is an ADMIN.
     */
    public static boolean isAdmin() {
        return TenantContext.hasRole(EmployeeRole.ADMIN);
    }
}
