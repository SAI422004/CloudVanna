package com.example.salesforcecrud.config;

import com.example.salesforcecrud.dto.FieldMetadata;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SalesforceObjectConfigTest {

    private final SalesforceObjectConfig config = new SalesforceObjectConfig();

    @Test
    void shouldReturnFiveSupportedObjects() {
        List<String> objects = config.getSupportedObjects();
        assertEquals(5, objects.size());
        assertTrue(objects.contains("Account"));
        assertTrue(objects.contains("Contact"));
        assertTrue(objects.contains("Lead"));
        assertTrue(objects.contains("Opportunity"));
        assertTrue(objects.contains("Case"));
    }

    @Test
    void shouldRecognizeSupportedObjects() {
        assertTrue(config.isSupported("Account"));
        assertTrue(config.isSupported("Contact"));
        assertTrue(config.isSupported("Lead"));
        assertTrue(config.isSupported("Opportunity"));
        assertTrue(config.isSupported("Case"));
    }

    @Test
    void shouldRejectUnsupportedObjects() {
        assertFalse(config.isSupported("User"));
        assertFalse(config.isSupported("CustomObject__c"));
        assertFalse(config.isSupported(""));
        assertFalse(config.isSupported(null));
    }

    @Test
    void shouldReturnFieldsForAccount() {
        List<FieldMetadata> fields = config.getFields("Account");
        assertFalse(fields.isEmpty());
        assertTrue(fields.size() >= 5);
        assertTrue(fields.size() <= 10);
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("Id")));
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("Name")));
    }

    @Test
    void shouldReturnEditableFieldsExcludingIdAndReadOnly() {
        List<FieldMetadata> editable = config.getEditableFields("Account");
        assertTrue(editable.stream().noneMatch(f -> f.getName().equals("Id")));
        assertTrue(editable.stream().allMatch(FieldMetadata::isEditable));
    }

    @Test
    void shouldReturnCommaSeparatedFieldList() {
        String fieldList = config.getFieldList("Account");
        assertNotNull(fieldList);
        assertTrue(fieldList.contains("Id"));
        assertTrue(fieldList.contains("Name"));
        assertTrue(fieldList.contains(","));
    }

    @Test
    void shouldReturnEmptyFieldsForUnsupportedObject() {
        List<FieldMetadata> fields = config.getFields("NonExistent");
        assertTrue(fields.isEmpty());
    }

    @Test
    void eachObjectShouldHaveBetween5And10Fields() {
        for (String obj : config.getSupportedObjects()) {
            List<FieldMetadata> fields = config.getFields(obj);
            assertTrue(fields.size() >= 5, obj + " should have at least 5 fields");
            assertTrue(fields.size() <= 10, obj + " should have at most 10 fields");
        }
    }

    @Test
    void eachObjectShouldHaveIdField() {
        for (String obj : config.getSupportedObjects()) {
            List<FieldMetadata> fields = config.getFields(obj);
            assertTrue(fields.stream().anyMatch(f -> "Id".equals(f.getName())),
                    obj + " should have an Id field");
        }
    }
}
