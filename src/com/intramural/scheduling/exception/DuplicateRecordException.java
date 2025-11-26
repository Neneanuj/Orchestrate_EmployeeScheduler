package com.intramural.scheduling.exception;

/**
 * REQ-005: Custom exception for duplicate record errors
 */
public class DuplicateRecordException extends RuntimeException {
    public DuplicateRecordException(String message) {
        super(message);
    }
    
    public DuplicateRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}
