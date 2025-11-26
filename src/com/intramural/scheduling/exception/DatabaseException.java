package com.intramural.scheduling.exception;

/**
 * REQ-005: Custom exception for database errors
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
