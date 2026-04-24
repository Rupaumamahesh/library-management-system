package com.library.exception;

/**
 * Exception thrown when a borrow operation violates business rules.
 * Examples: borrow limit exceeded, book not available.
 * Mapped to HTTP 400 response.
 */
public class BorrowLimitException extends RuntimeException {

    public BorrowLimitException(String message) {
        super(message);
    }

    public BorrowLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
