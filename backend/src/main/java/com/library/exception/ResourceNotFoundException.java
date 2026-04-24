package com.library.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Mapped to HTTP 404 response.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
