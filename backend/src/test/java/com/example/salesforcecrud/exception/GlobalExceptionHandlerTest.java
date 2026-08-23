package com.example.salesforcecrud.exception;

import com.example.salesforcecrud.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturn401ForAuthenticationException() {
        AuthenticationException ex = new AuthenticationException("Not authenticated");
        ResponseEntity<ErrorResponse> response = handler.handleAuthenticationException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Not authenticated", response.getBody().getMessage());
    }

    @Test
    void shouldReturn400ForUnsupportedObject() {
        UnsupportedObjectException ex = new UnsupportedObjectException("BadObject");
        ResponseEntity<ErrorResponse> response = handler.handleUnsupportedObject(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("BadObject"));
    }

    @Test
    void shouldReturn400ForSalesforceApiError400() {
        SalesforceApiException ex = new SalesforceApiException("Missing field", 400);
        ResponseEntity<ErrorResponse> response = handler.handleSalesforceApiException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing field", response.getBody().getMessage());
    }

    @Test
    void shouldReturn404ForSalesforceApiError404() {
        SalesforceApiException ex = new SalesforceApiException("Record not found", 404);
        ResponseEntity<ErrorResponse> response = handler.handleSalesforceApiException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturn500ForSalesforceApiError500WithSanitizedMessage() {
        SalesforceApiException ex = new SalesforceApiException("Internal Salesforce Error Details", 500);
        ResponseEntity<ErrorResponse> response = handler.handleSalesforceApiException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        // Should NOT expose internal message
        assertFalse(response.getBody().getMessage().contains("Internal Salesforce Error Details"));
    }

    @Test
    void shouldReturn400ForIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid ID format");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid ID format", response.getBody().getMessage());
    }

    @Test
    void shouldReturn500ForGenericException() {
        Exception ex = new RuntimeException("Unexpected");
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        // Should NOT expose internal details
        assertFalse(response.getBody().getMessage().contains("Unexpected"));
    }

    @Test
    void errorResponseShouldContainTimestamp() {
        AuthenticationException ex = new AuthenticationException("Test");
        ResponseEntity<ErrorResponse> response = handler.handleAuthenticationException(ex);

        assertNotNull(response.getBody().getTimestamp());
    }
}
