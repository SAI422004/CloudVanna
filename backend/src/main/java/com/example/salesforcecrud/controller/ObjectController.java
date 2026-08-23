package com.example.salesforcecrud.controller;

import com.example.salesforcecrud.dto.ObjectMetadataResponse;
import com.example.salesforcecrud.service.RecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Salesforce object metadata.
 */
@RestController
@RequestMapping("/api/objects")
public class ObjectController {

    private final RecordService recordService;

    public ObjectController(RecordService recordService) {
        this.recordService = recordService;
    }

    /**
     * Get the list of supported Salesforce objects.
     */
    @GetMapping
    public ResponseEntity<Map<String, List<String>>> getSupportedObjects() {
        List<String> objects = recordService.getSupportedObjects();
        return ResponseEntity.ok(Map.of("objects", objects));
    }

    /**
     * Get field metadata for a specific object.
     */
    @GetMapping("/{objectName}/metadata")
    public ResponseEntity<ObjectMetadataResponse> getObjectMetadata(
            @PathVariable String objectName) {
        ObjectMetadataResponse metadata = recordService.getObjectMetadata(objectName);
        return ResponseEntity.ok(metadata);
    }
}
