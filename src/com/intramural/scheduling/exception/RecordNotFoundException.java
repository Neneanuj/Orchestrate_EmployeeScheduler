package com.intramural.scheduling.exception;

/**
 * REQ-005: Custom exception for record not found errors
 */
public class RecordNotFoundException extends RuntimeException {
    public RecordNotFoundException(String message) {
        super(message);
    }
    
    public RecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
