package com.example.salesforcecrud.controller;

import com.example.salesforcecrud.dto.PagedRecordResponse;
import com.example.salesforcecrud.model.SalesforceToken;
import com.example.salesforcecrud.service.RecordService;
import com.example.salesforcecrud.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for Salesforce record CRUD operations.
 */
@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    /**
     * Query records with cursor-based pagination.
     * Pass cursor parameter for subsequent pages (value from nextPageUrl in response).
     */
    @GetMapping("/{objectName}")
    public ResponseEntity<PagedRecordResponse> getRecords(
            @PathVariable String objectName,
            @RequestParam(required = false) String cursor,
            HttpSession session) {

        SalesforceToken token = SessionUtil.getToken(session);
        PagedRecordResponse response = recordService.getRecords(token, objectName, cursor);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a single record by ID.
     */
    @GetMapping("/{objectName}/{id}")
    public ResponseEntity<Map<String, Object>> getRecord(
            @PathVariable String objectName,
            @PathVariable String id,
            HttpSession session) {

        SalesforceToken token = SessionUtil.getToken(session);
        Map<String, Object> record = recordService.getRecord(token, objectName, id);
        return ResponseEntity.ok(record);
    }

    /**
     * Create a new record.
     */
    @PostMapping("/{objectName}")
    public ResponseEntity<Map<String, Object>> createRecord(
            @PathVariable String objectName,
            @RequestBody Map<String, Object> fields,
            HttpSession session) {

        SalesforceToken token = SessionUtil.getToken(session);
        Map<String, Object> result = recordService.createRecord(token, objectName, fields);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Update an existing record.
     */
    @PatchMapping("/{objectName}/{id}")
    public ResponseEntity<Map<String, String>> updateRecord(
            @PathVariable String objectName,
            @PathVariable String id,
            @RequestBody Map<String, Object> fields,
            HttpSession session) {

        SalesforceToken token = SessionUtil.getToken(session);
        recordService.updateRecord(token, objectName, id, fields);
        return ResponseEntity.ok(Map.of("message", "Record updated successfully"));
    }

    /**
     * Delete a record.
     */
    @DeleteMapping("/{objectName}/{id}")
    public ResponseEntity<Map<String, String>> deleteRecord(
            @PathVariable String objectName,
            @PathVariable String id,
            HttpSession session) {

        SalesforceToken token = SessionUtil.getToken(session);
        recordService.deleteRecord(token, objectName, id);
        return ResponseEntity.ok(Map.of("message", "Record deleted successfully"));
    }
}
