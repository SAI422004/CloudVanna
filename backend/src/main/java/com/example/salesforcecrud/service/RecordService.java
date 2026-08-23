package com.example.salesforcecrud.service;

import com.example.salesforcecrud.client.SalesforceRestClient;
import com.example.salesforcecrud.config.SalesforceObjectConfig;
import com.example.salesforcecrud.dto.FieldMetadata;
import com.example.salesforcecrud.dto.ObjectMetadataResponse;
import com.example.salesforcecrud.dto.PagedRecordResponse;
import com.example.salesforcecrud.exception.SalesforceApiException;
import com.example.salesforcecrud.exception.UnsupportedObjectException;
import com.example.salesforcecrud.model.SalesforceToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for Salesforce record operations.
 * Validates object names against the whitelist and delegates to SalesforceRestClient.
 */
@Service
public class RecordService {

    private static final Logger log = LoggerFactory.getLogger(RecordService.class);

    private final SalesforceRestClient salesforceClient;
    private final SalesforceObjectConfig objectConfig;

    public RecordService(SalesforceRestClient salesforceClient, SalesforceObjectConfig objectConfig) {
        this.salesforceClient = salesforceClient;
        this.objectConfig = objectConfig;
    }

    /**
     * Get the list of supported Salesforce object names.
     */
    public List<String> getSupportedObjects() {
        return objectConfig.getSupportedObjects();
    }

    /**
     * Get metadata (fields) for a given Salesforce object.
     */
    public ObjectMetadataResponse getObjectMetadata(String objectName) {
        validateObjectName(objectName);
        List<FieldMetadata> allFields = objectConfig.getFields(objectName);
        List<FieldMetadata> editableFields = objectConfig.getEditableFields(objectName);
        return new ObjectMetadataResponse(objectName, allFields, editableFields);
    }

    /**
     * Query records with cursor-based pagination.
     *
     * @param token      Salesforce token from session
     * @param objectName Salesforce object name
     * @param cursor     Optional nextRecordsUrl for pagination
     * @return Paginated response
     */
    @SuppressWarnings("unchecked")
    public PagedRecordResponse getRecords(SalesforceToken token, String objectName, String cursor) {
        validateObjectName(objectName);
        String fieldList = objectConfig.getFieldList(objectName);

        Map<String, Object> response = salesforceClient.queryRecords(token, objectName, fieldList, cursor);

        List<Map<String, Object>> rawRecords = (List<Map<String, Object>>) response.get("records");
        int totalSize = response.get("totalSize") != null ? ((Number) response.get("totalSize")).intValue() : 0;
        boolean done = Boolean.TRUE.equals(response.get("done"));
        String nextRecordsUrl = (String) response.get("nextRecordsUrl");

        // Clean records — remove Salesforce internal attributes
        List<Map<String, Object>> cleanedRecords = rawRecords.stream()
                .map(this::cleanRecord)
                .collect(Collectors.toList());

        return new PagedRecordResponse(cleanedRecords, totalSize, done, nextRecordsUrl);
    }

    /**
     * Get a single record by ID.
     */
    public Map<String, Object> getRecord(SalesforceToken token, String objectName, String recordId) {
        validateObjectName(objectName);
        validateRecordId(recordId);
        String fieldList = objectConfig.getFieldList(objectName);

        Map<String, Object> record = salesforceClient.getRecord(token, objectName, recordId, fieldList);
        return cleanRecord(record);
    }

    /**
     * Create a new record.
     *
     * @return Map containing the created record ID
     */
    public Map<String, Object> createRecord(SalesforceToken token, String objectName,
                                            Map<String, Object> fields) {
        validateObjectName(objectName);
        Map<String, Object> sanitizedFields = sanitizeFields(objectName, fields);
        validateRequiredFields(objectName, sanitizedFields);

        String id = salesforceClient.createRecord(token, objectName, sanitizedFields);
        return Map.of("id", id, "success", true);
    }

    /**
     * Update an existing record.
     */
    public void updateRecord(SalesforceToken token, String objectName,
                             String recordId, Map<String, Object> fields) {
        validateObjectName(objectName);
        validateRecordId(recordId);
        Map<String, Object> sanitizedFields = sanitizeFields(objectName, fields);

        salesforceClient.updateRecord(token, objectName, recordId, sanitizedFields);
    }

    /**
     * Delete a record.
     */
    public void deleteRecord(SalesforceToken token, String objectName, String recordId) {
        validateObjectName(objectName);
        validateRecordId(recordId);

        salesforceClient.deleteRecord(token, objectName, recordId);
    }

    // --- Validation helpers ---

    private void validateObjectName(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            throw new IllegalArgumentException("Object name is required");
        }
        if (!objectConfig.isSupported(objectName)) {
            throw new UnsupportedObjectException(objectName);
        }
    }

    private void validateRecordId(String recordId) {
        if (recordId == null || recordId.isBlank()) {
            throw new IllegalArgumentException("Record ID is required");
        }
        // Salesforce IDs are 15 or 18 characters, alphanumeric
        if (!recordId.matches("^[a-zA-Z0-9]{15,18}$")) {
            throw new IllegalArgumentException("Invalid Salesforce record ID format");
        }
    }

    /**
     * Only allow fields that are in the whitelist and editable.
     */
    private Map<String, Object> sanitizeFields(String objectName, Map<String, Object> fields) {
        Set<String> editableFieldNames = objectConfig.getEditableFields(objectName).stream()
                .map(FieldMetadata::getName)
                .collect(Collectors.toSet());

        Map<String, Object> sanitized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (editableFieldNames.contains(entry.getKey())) {
                sanitized.put(entry.getKey(), entry.getValue());
            } else {
                log.warn("Ignoring non-editable field '{}' for object '{}'", entry.getKey(), objectName);
            }
        }
        return sanitized;
    }

    /**
     * Validate that all required fields are present and non-empty.
     */
    private void validateRequiredFields(String objectName, Map<String, Object> fields) {
        List<String> missingFields = objectConfig.getFields(objectName).stream()
                .filter(FieldMetadata::isRequired)
                .filter(FieldMetadata::isEditable)
                .filter(f -> {
                    Object value = fields.get(f.getName());
                    return value == null || (value instanceof String s && s.isBlank());
                })
                .map(f -> f.getLabel() + " (" + f.getName() + ")")
                .toList();

        if (!missingFields.isEmpty()) {
            throw new SalesforceApiException(
                    "Missing required fields: " + String.join(", ", missingFields), 400);
        }
    }

    /**
     * Remove Salesforce internal attributes (like "attributes" key) from a record.
     */
    private Map<String, Object> cleanRecord(Map<String, Object> record) {
        if (record == null) return Collections.emptyMap();
        Map<String, Object> cleaned = new LinkedHashMap<>(record);
        cleaned.remove("attributes");
        return cleaned;
    }
}
