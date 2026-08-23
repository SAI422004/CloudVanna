package com.example.salesforcecrud.service;

import com.example.salesforcecrud.client.SalesforceRestClient;
import com.example.salesforcecrud.config.SalesforceObjectConfig;
import com.example.salesforcecrud.dto.ObjectMetadataResponse;
import com.example.salesforcecrud.dto.PagedRecordResponse;
import com.example.salesforcecrud.exception.SalesforceApiException;
import com.example.salesforcecrud.exception.UnsupportedObjectException;
import com.example.salesforcecrud.model.SalesforceToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private SalesforceRestClient salesforceClient;

    private SalesforceObjectConfig objectConfig;

    private RecordService recordService;

    private SalesforceToken token;

    @BeforeEach
    void setUp() {
        objectConfig = new SalesforceObjectConfig();
        recordService = new RecordService(salesforceClient, objectConfig);

        token = new SalesforceToken();
        token.setAccessToken("test-token");
        token.setInstanceUrl("https://test.salesforce.com");
    }

    @Test
    void getSupportedObjectsShouldReturnFiveObjects() {
        List<String> objects = recordService.getSupportedObjects();
        assertEquals(5, objects.size());
    }

    @Test
    void getObjectMetadataShouldReturnFieldsForAccount() {
        ObjectMetadataResponse metadata = recordService.getObjectMetadata("Account");
        assertEquals("Account", metadata.getObjectName());
        assertFalse(metadata.getFields().isEmpty());
        assertFalse(metadata.getEditableFields().isEmpty());
    }

    @Test
    void getObjectMetadataShouldThrowForUnsupportedObject() {
        assertThrows(UnsupportedObjectException.class,
                () -> recordService.getObjectMetadata("Invalid"));
    }

    @Test
    void getRecordsShouldDelegateToClient() {
        Map<String, Object> sfResponse = new HashMap<>();
        sfResponse.put("records", List.of(Map.of("Id", "001xx", "Name", "Test")));
        sfResponse.put("totalSize", 1);
        sfResponse.put("done", true);

        when(salesforceClient.queryRecords(eq(token), eq("Account"), anyString(), isNull()))
                .thenReturn(sfResponse);

        PagedRecordResponse response = recordService.getRecords(token, "Account", null);

        assertNotNull(response);
        assertEquals(1, response.getRecords().size());
        assertEquals(1, response.getTotalSize());
        assertTrue(response.isDone());
        verify(salesforceClient).queryRecords(eq(token), eq("Account"), anyString(), isNull());
    }

    @Test
    void getRecordsShouldThrowForUnsupportedObject() {
        assertThrows(UnsupportedObjectException.class,
                () -> recordService.getRecords(token, "BadObject", null));
    }

    @Test
    void getRecordShouldValidateRecordId() {
        assertThrows(IllegalArgumentException.class,
                () -> recordService.getRecord(token, "Account", "invalid!id"));
    }

    @Test
    void getRecordShouldAcceptValid15CharId() {
        Map<String, Object> record = Map.of("Id", "001000000000AAA", "Name", "Test");
        when(salesforceClient.getRecord(eq(token), eq("Account"), eq("001000000000AAA"), anyString()))
                .thenReturn(record);

        Map<String, Object> result = recordService.getRecord(token, "Account", "001000000000AAA");
        assertNotNull(result);
        assertEquals("Test", result.get("Name"));
    }

    @Test
    void createRecordShouldValidateRequiredFields() {
        Map<String, Object> fields = new HashMap<>();
        // Account requires Name
        assertThrows(SalesforceApiException.class,
                () -> recordService.createRecord(token, "Account", fields));
    }

    @Test
    void createRecordShouldSanitizeFields() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("Name", "Test Account");
        fields.put("HackerField", "evil"); // Not in whitelist

        when(salesforceClient.createRecord(eq(token), eq("Account"), anyMap()))
                .thenReturn("001new");

        Map<String, Object> result = recordService.createRecord(token, "Account", fields);

        assertEquals("001new", result.get("id"));
        verify(salesforceClient).createRecord(eq(token), eq("Account"), argThat(map ->
                map.containsKey("Name") && !map.containsKey("HackerField")));
    }

    @Test
    void updateRecordShouldDelegateToClient() {
        Map<String, Object> fields = Map.of("Name", "Updated");
        doNothing().when(salesforceClient).updateRecord(eq(token), eq("Account"), eq("001000000000AAA"), anyMap());

        assertDoesNotThrow(
                () -> recordService.updateRecord(token, "Account", "001000000000AAA", fields));
        verify(salesforceClient).updateRecord(eq(token), eq("Account"), eq("001000000000AAA"), anyMap());
    }

    @Test
    void deleteRecordShouldDelegateToClient() {
        doNothing().when(salesforceClient).deleteRecord(token, "Account", "001000000000AAA");

        assertDoesNotThrow(
                () -> recordService.deleteRecord(token, "Account", "001000000000AAA"));
        verify(salesforceClient).deleteRecord(token, "Account", "001000000000AAA");
    }

    @Test
    void deleteRecordShouldThrowForUnsupportedObject() {
        assertThrows(UnsupportedObjectException.class,
                () -> recordService.deleteRecord(token, "BadObject", "001000000000AAA"));
    }

    @Test
    void shouldRejectNullObjectName() {
        assertThrows(IllegalArgumentException.class,
                () -> recordService.getRecords(token, null, null));
    }

    @Test
    void shouldRejectBlankObjectName() {
        assertThrows(IllegalArgumentException.class,
                () -> recordService.getRecords(token, "   ", null));
    }
}
