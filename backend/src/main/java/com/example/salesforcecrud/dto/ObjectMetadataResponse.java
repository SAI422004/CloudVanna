package com.example.salesforcecrud.dto;

import java.util.List;

/**
 * Response containing object metadata (supported objects or field definitions).
 */
public class ObjectMetadataResponse {

    private String objectName;
    private List<FieldMetadata> fields;
    private List<FieldMetadata> editableFields;

    public ObjectMetadataResponse() {}

    public ObjectMetadataResponse(String objectName, List<FieldMetadata> fields, List<FieldMetadata> editableFields) {
        this.objectName = objectName;
        this.fields = fields;
        this.editableFields = editableFields;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public List<FieldMetadata> getFields() {
        return fields;
    }

    public void setFields(List<FieldMetadata> fields) {
        this.fields = fields;
    }

    public List<FieldMetadata> getEditableFields() {
        return editableFields;
    }

    public void setEditableFields(List<FieldMetadata> editableFields) {
        this.editableFields = editableFields;
    }
}
