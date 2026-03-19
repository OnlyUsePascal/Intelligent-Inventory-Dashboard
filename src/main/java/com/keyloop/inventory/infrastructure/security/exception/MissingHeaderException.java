package com.keyloop.inventory.infrastructure.security.exception;

/**
 * Exception thrown when a required HTTP header is missing.
 * Maps to HTTP 400 Bad Request.
 */
public class MissingHeaderException extends RuntimeException {

    private final String headerName;

    public MissingHeaderException(String headerName) {
        super("Missing required header: " + headerName);
        this.headerName = headerName;
    }

    public MissingHeaderException(String headerName, String message) {
        super(message);
        this.headerName = headerName;
    }

    public String getHeaderName() {
        return headerName;
    }
}
