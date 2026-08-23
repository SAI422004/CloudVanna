package com.example.salesforcecrud.client;

import com.example.salesforcecrud.exception.AuthenticationException;
import com.example.salesforcecrud.exception.SalesforceApiException;
import com.example.salesforcecrud.model.SalesforceToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Client for making REST API calls to Salesforce.
 * All Salesforce API communication is centralized here.
 */
@Component
public class SalesforceRestClient {

    private static final Logger log = LoggerFactory.getLogger(SalesforceRestClient.class);
    private static final String API_VERSION = "v60.0";
    private static final int PAGE_SIZE = 20;

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public SalesforceRestClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
    }

    /**
     * Query records using SOQL with cursor-based pagination.
     *
     * @param token      The Salesforce access token
     * @param objectName The Salesforce object (e.g. Account)
     * @param fieldList  Comma-separated field names
     * @param nextUrl    Optional nextRecordsUrl for pagination (null for first page)
     * @return Raw response map from Salesforce (records, totalSize, done, nextRecordsUrl)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> queryRecords(SalesforceToken token, String objectName,
                                            String fieldList, String nextUrl) {
        String url;
        if (nextUrl != null && !nextUrl.isBlank()) {
            // Use the nextRecordsUrl for subsequent pages
            url = token.getInstanceUrl() + nextUrl;
            log.debug("Fetching next page: {}", nextUrl);
        } else {
            // Build initial SOQL query
            String soql = String.format("SELECT %s FROM %s ORDER BY Id ASC LIMIT %d",
                    fieldList, objectName, PAGE_SIZE);
            url = token.getInstanceUrl() + "/services/data/" + API_VERSION + "/query?q=" + encodeQuery(soql);
            log.debug("Executing SOQL query for {} with fields: {}", objectName, fieldList);
        }

        try {
            String responseBody = buildClient(token)
                    .get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new SalesforceApiException(
                                                    extractErrorMessage(body),
                                                    resp.statusCode().value()))))
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(responseBody, new TypeReference<>() {});
        } catch (SalesforceApiException e) {
            throw e;
        } catch (WebClientResponseException e) {
            throw mapWebClientException(e);
        } catch (Exception e) {
            if (e instanceof SalesforceApiException) throw (SalesforceApiException) e;
            log.error("Error querying Salesforce: {}", e.getMessage());
            throw new SalesforceApiException("Failed to query Salesforce records", e);
        }
    }

    /**
     * Get a single record by ID.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getRecord(SalesforceToken token, String objectName,
                                         String recordId, String fieldList) {
        String url = token.getInstanceUrl() + "/services/data/" + API_VERSION
                + "/sobjects/" + objectName + "/" + recordId
                + "?fields=" + fieldList;

        log.debug("Fetching {} record: {}", objectName, recordId);

        try {
            String responseBody = buildClient(token)
                    .get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new SalesforceApiException(
                                                    extractErrorMessage(body),
                                                    resp.statusCode().value()))))
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(responseBody, new TypeReference<>() {});
        } catch (SalesforceApiException e) {
            throw e;
        } catch (WebClientResponseException e) {
            throw mapWebClientException(e);
        } catch (Exception e) {
            if (e instanceof SalesforceApiException) throw (SalesforceApiException) e;
            log.error("Error getting Salesforce record: {}", e.getMessage());
            throw new SalesforceApiException("Failed to get Salesforce record", e);
        }
    }

    /**
     * Create a new record.
     *
     * @return The created record ID
     */
    @SuppressWarnings("unchecked")
    public String createRecord(SalesforceToken token, String objectName,
                               Map<String, Object> fields) {
        String url = token.getInstanceUrl() + "/services/data/" + API_VERSION
                + "/sobjects/" + objectName;

        log.debug("Creating {} record", objectName);

        try {
            String responseBody = buildClient(token)
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(fields)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new SalesforceApiException(
                                                    extractErrorMessage(body),
                                                    resp.statusCode().value()))))
                    .bodyToMono(String.class)
                    .block();

            Map<String, Object> result = objectMapper.readValue(responseBody, new TypeReference<>() {});
            Boolean success = (Boolean) result.get("success");
            if (Boolean.TRUE.equals(success)) {
                String id = (String) result.get("id");
                log.info("Created {} record: {}", objectName, id);
                return id;
            } else {
                List<Map<String, Object>> errors = (List<Map<String, Object>>) result.get("errors");
                String errorMsg = errors != null && !errors.isEmpty()
                        ? errors.get(0).getOrDefault("message", "Unknown error").toString()
                        : "Record creation failed";
                throw new SalesforceApiException(errorMsg, 400);
            }
        } catch (SalesforceApiException e) {
            throw e;
        } catch (WebClientResponseException e) {
            throw mapWebClientException(e);
        } catch (Exception e) {
            if (e instanceof SalesforceApiException) throw (SalesforceApiException) e;
            log.error("Error creating Salesforce record: {}", e.getMessage());
            throw new SalesforceApiException("Failed to create Salesforce record", e);
        }
    }

    /**
     * Update an existing record.
     */
    public void updateRecord(SalesforceToken token, String objectName,
                             String recordId, Map<String, Object> fields) {
        String url = token.getInstanceUrl() + "/services/data/" + API_VERSION
                + "/sobjects/" + objectName + "/" + recordId;

        log.debug("Updating {} record: {}", objectName, recordId);

        try {
            buildClient(token)
                    .patch()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(fields)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new SalesforceApiException(
                                                    extractErrorMessage(body),
                                                    resp.statusCode().value()))))
                    .bodyToMono(String.class)
                    .defaultIfEmpty("") // Salesforce returns 204 No Content on success
                    .block();

            log.info("Updated {} record: {}", objectName, recordId);
        } catch (SalesforceApiException e) {
            throw e;
        } catch (WebClientResponseException e) {
            throw mapWebClientException(e);
        } catch (Exception e) {
            if (e instanceof SalesforceApiException) throw (SalesforceApiException) e;
            log.error("Error updating Salesforce record: {}", e.getMessage());
            throw new SalesforceApiException("Failed to update Salesforce record", e);
        }
    }

    /**
     * Delete a record.
     */
    public void deleteRecord(SalesforceToken token, String objectName, String recordId) {
        String url = token.getInstanceUrl() + "/services/data/" + API_VERSION
                + "/sobjects/" + objectName + "/" + recordId;

        log.debug("Deleting {} record: {}", objectName, recordId);

        try {
            buildClient(token)
                    .delete()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new SalesforceApiException(
                                                    extractErrorMessage(body),
                                                    resp.statusCode().value()))))
                    .bodyToMono(String.class)
                    .defaultIfEmpty("") // Salesforce returns 204 No Content on success
                    .block();

            log.info("Deleted {} record: {}", objectName, recordId);
        } catch (SalesforceApiException e) {
            throw e;
        } catch (WebClientResponseException e) {
            throw mapWebClientException(e);
        } catch (Exception e) {
            if (e instanceof SalesforceApiException) throw (SalesforceApiException) e;
            log.error("Error deleting Salesforce record: {}", e.getMessage());
            throw new SalesforceApiException("Failed to delete Salesforce record", e);
        }
    }

    // --- Private helpers ---

    private WebClient buildClient(SalesforceToken token) {
        if (token == null || token.getAccessToken() == null) {
            throw new AuthenticationException("No valid Salesforce token. Please login.");
        }
        return webClientBuilder
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
                .build();
    }

    private String encodeQuery(String soql) {
        return soql.replace(" ", "+");
    }

    /**
     * Extract a clean error message from a Salesforce error response body.
     */
    @SuppressWarnings("unchecked")
    private String extractErrorMessage(String responseBody) {
        try {
            // Salesforce errors can be an array: [{"message": "...", "errorCode": "..."}]
            if (responseBody.trim().startsWith("[")) {
                List<Map<String, Object>> errors = objectMapper.readValue(responseBody, new TypeReference<>() {});
                if (!errors.isEmpty()) {
                    return errors.get(0).getOrDefault("message", "Salesforce error").toString();
                }
            }
            // Or a single object: {"error": "...", "error_description": "..."}
            Map<String, Object> error = objectMapper.readValue(responseBody, new TypeReference<>() {});
            if (error.containsKey("error_description")) {
                return error.get("error_description").toString();
            }
            if (error.containsKey("message")) {
                return error.get("message").toString();
            }
        } catch (Exception ignored) {
            // Fall through to default
        }
        return "Salesforce API error";
    }

    private SalesforceApiException mapWebClientException(WebClientResponseException e) {
        String message = extractErrorMessage(e.getResponseBodyAsString());
        return new SalesforceApiException(message, e.getStatusCode().value());
    }
}
