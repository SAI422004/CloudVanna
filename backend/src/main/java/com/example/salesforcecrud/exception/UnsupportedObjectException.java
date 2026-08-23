package com.example.salesforcecrud.exception;

/**
 * Exception thrown when an unsupported Salesforce object is requested.
 */
public class UnsupportedObjectException extends RuntimeException {

    public UnsupportedObjectException(String objectName) {
        super("Unsupported Salesforce object: " + objectName);
    }
}
