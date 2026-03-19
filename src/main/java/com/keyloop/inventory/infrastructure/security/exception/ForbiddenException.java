package com.keyloop.inventory.infrastructure.security.exception;

/**
 * Exception thrown when user lacks permission for an operation.
 * Maps to HTTP 403 Forbidden.
 */
public class ForbiddenException extends RuntimeException {

    private final String requiredRoles;

    public ForbiddenException(String message) {
        super(message);
        this.requiredRoles = null;
    }

    public ForbiddenException(String message, String requiredRoles) {
        super(message);
        this.requiredRoles = requiredRoles;
    }

    public String getRequiredRoles() {
        return requiredRoles;
    }

    public static ForbiddenException insufficientPermissions() {
        return new ForbiddenException("Insufficient permissions for this operation");
    }

    public static ForbiddenException requiresRole(String... roles) {
        String rolesStr = String.join(", ", roles);
        return new ForbiddenException(
                "This operation requires one of the following roles: " + rolesStr,
                rolesStr
        );
    }
}
