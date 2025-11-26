package com.intramural.scheduling.exception;

/**
 * REQ-005: Custom exception for scheduling conflicts (double-booking, etc.)
 */
public class SchedulingConflictException extends RuntimeException {
    public SchedulingConflictException(String message) {
        super(message);
    }
    
    public SchedulingConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
