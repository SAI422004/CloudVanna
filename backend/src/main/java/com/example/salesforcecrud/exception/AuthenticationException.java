package com.example.salesforcecrud.exception;

/**
 * Exception thrown when the user is not authenticated or the token has expired.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
