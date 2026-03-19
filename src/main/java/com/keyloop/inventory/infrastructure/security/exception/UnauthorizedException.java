package com.keyloop.inventory.infrastructure.security.exception;

/**
 * Exception thrown when authentication fails (invalid tenant or employee).
 * Maps to HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public static UnauthorizedException invalidTenant() {
        return new UnauthorizedException("Invalid or unknown tenant");
    }

    public static UnauthorizedException invalidEmployee() {
        return new UnauthorizedException("Invalid employee or employee does not belong to tenant");
    }

    public static UnauthorizedException roleMismatch() {
        return new UnauthorizedException("Employee role does not match the provided role");
    }
}
