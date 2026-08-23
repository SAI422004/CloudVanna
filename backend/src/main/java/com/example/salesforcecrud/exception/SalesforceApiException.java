package com.example.salesforcecrud.exception;

/**
 * Exception thrown when Salesforce API returns an error.
 */
public class SalesforceApiException extends RuntimeException {

    private final int statusCode;
    private final String errorCode;

    public SalesforceApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = null;
    }

    public SalesforceApiException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public SalesforceApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
        this.errorCode = null;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
