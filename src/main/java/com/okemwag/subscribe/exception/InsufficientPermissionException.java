package com.okemwag.subscribe.exception;

/**
 * Exception thrown when a user attempts to access resources they don't have permission for
 */
public class InsufficientPermissionException extends SubscribeException {
    
    public InsufficientPermissionException(String message) {
        super(message);
    }
    
    public InsufficientPermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}