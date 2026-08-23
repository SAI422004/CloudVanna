package com.example.salesforcecrud.dto;

/**
 * Metadata describing a Salesforce field.
 */
public class FieldMetadata {

    private String name;
    private String label;
    private String type;
    private boolean required;
    private boolean editable;

    public FieldMetadata() {}

    public FieldMetadata(String name, String label, String type, boolean required, boolean editable) {
        this.name = name;
        this.label = label;
        this.type = type;
        this.required = required;
        this.editable = editable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
