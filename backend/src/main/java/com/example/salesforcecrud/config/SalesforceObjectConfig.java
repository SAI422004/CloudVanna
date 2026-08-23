package com.example.salesforcecrud.config;

import com.example.salesforcecrud.dto.FieldMetadata;
import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * Whitelist of supported Salesforce objects and their field definitions.
 * This is the single source of truth for which objects and fields are accessible.
 */
@Configuration
public class SalesforceObjectConfig {

    private static final Map<String, List<FieldMetadata>> OBJECT_FIELDS = new LinkedHashMap<>();

    static {
        // Account fields
        OBJECT_FIELDS.put("Account", List.of(
                new FieldMetadata("Id", "Id", "id", false, false),
                new FieldMetadata("Name", "Account Name", "string", true, true),
                new FieldMetadata("Phone", "Phone", "phone", false, true),
                new FieldMetadata("Website", "Website", "url", false, true),
                new FieldMetadata("Industry", "Industry", "picklist", false, true),
                new FieldMetadata("Type", "Type", "picklist", false, true),
                new FieldMetadata("BillingCity", "Billing City", "string", false, true),
                new FieldMetadata("AnnualRevenue", "Annual Revenue", "currency", false, true)
        ));

        // Contact fields
        OBJECT_FIELDS.put("Contact", List.of(
                new FieldMetadata("Id", "Id", "id", false, false),
                new FieldMetadata("FirstName", "First Name", "string", false, true),
                new FieldMetadata("LastName", "Last Name", "string", true, true),
                new FieldMetadata("Email", "Email", "email", false, true),
                new FieldMetadata("Phone", "Phone", "phone", false, true),
                new FieldMetadata("Department", "Department", "string", false, true),
                new FieldMetadata("Title", "Title", "string", false, true),
                new FieldMetadata("MailingCity", "Mailing City", "string", false, true)
        ));

        // Lead fields
        OBJECT_FIELDS.put("Lead", List.of(
                new FieldMetadata("Id", "Id", "id", false, false),
                new FieldMetadata("FirstName", "First Name", "string", false, true),
                new FieldMetadata("LastName", "Last Name", "string", true, true),
                new FieldMetadata("Company", "Company", "string", true, true),
                new FieldMetadata("Email", "Email", "email", false, true),
                new FieldMetadata("Phone", "Phone", "phone", false, true),
                new FieldMetadata("Status", "Status", "picklist", true, true),
                new FieldMetadata("LeadSource", "Lead Source", "picklist", false, true)
        ));

        // Opportunity fields
        OBJECT_FIELDS.put("Opportunity", List.of(
                new FieldMetadata("Id", "Id", "id", false, false),
                new FieldMetadata("Name", "Opportunity Name", "string", true, true),
                new FieldMetadata("Amount", "Amount", "currency", false, true),
                new FieldMetadata("StageName", "Stage", "picklist", true, true),
                new FieldMetadata("CloseDate", "Close Date", "date", true, true),
                new FieldMetadata("Probability", "Probability (%)", "percent", false, true),
                new FieldMetadata("Type", "Type", "picklist", false, true)
        ));

        // Case fields
        OBJECT_FIELDS.put("Case", List.of(
                new FieldMetadata("Id", "Id", "id", false, false),
                new FieldMetadata("CaseNumber", "Case Number", "string", false, false),
                new FieldMetadata("Subject", "Subject", "string", false, true),
                new FieldMetadata("Status", "Status", "picklist", false, true),
                new FieldMetadata("Priority", "Priority", "picklist", false, true),
                new FieldMetadata("Origin", "Case Origin", "picklist", false, true),
                new FieldMetadata("Description", "Description", "textarea", false, true),
                new FieldMetadata("Type", "Type", "picklist", false, true)
        ));
    }

    /**
     * Check if an object name is in the whitelist.
     */
    public boolean isSupported(String objectName) {
        return OBJECT_FIELDS.containsKey(objectName);
    }

    /**
     * Get the list of supported object names.
     */
    public List<String> getSupportedObjects() {
        return new ArrayList<>(OBJECT_FIELDS.keySet());
    }

    /**
     * Get field metadata for a supported object.
     */
    public List<FieldMetadata> getFields(String objectName) {
        return OBJECT_FIELDS.getOrDefault(objectName, Collections.emptyList());
    }

    /**
     * Get the SOQL-compatible field list for a given object (comma-separated).
     */
    public String getFieldList(String objectName) {
        return getFields(objectName).stream()
                .map(FieldMetadata::getName)
                .reduce((a, b) -> a + "," + b)
                .orElse("Id");
    }

    /**
     * Get only the editable fields (for create/update forms).
     */
    public List<FieldMetadata> getEditableFields(String objectName) {
        return getFields(objectName).stream()
                .filter(FieldMetadata::isEditable)
                .toList();
    }
}
