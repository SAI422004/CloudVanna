package com.example.salesforcecrud.service;

import com.example.salesforcecrud.config.SalesforceConfig;
import com.example.salesforcecrud.exception.AuthenticationException;
import com.example.salesforcecrud.model.SalesforceToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Handles Salesforce OAuth 2.0 token exchange and refresh.
 */
@Service
public class OAuthService {

    private static final Logger log = LoggerFactory.getLogger(OAuthService.class);

    private final SalesforceConfig salesforceConfig;
    private final WebClient webClient;

    public OAuthService(SalesforceConfig salesforceConfig) {
        this.salesforceConfig = salesforceConfig;
        this.webClient = WebClient.builder().build();
    }

    /**
     * Generate a cryptographically random code verifier for PKCE (RFC 7636).
     */
    public static String generateCodeVerifier() {
        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    /**
     * Generate the S256 code challenge from a code verifier.
     */
    public static String generateCodeChallenge(String codeVerifier) {
        try {
            byte[] bytes = codeVerifier.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
            java.security.MessageDigest messageDigest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(bytes);
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Exchange authorization code for access and refresh tokens (with PKCE code verifier).
     */
    public SalesforceToken exchangeCodeForToken(String authorizationCode, String codeVerifier) {
        log.debug("Exchanging authorization code for token with PKCE");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", authorizationCode);
        formData.add("client_id", salesforceConfig.getClientId());
        if (salesforceConfig.getClientSecret() != null && !salesforceConfig.getClientSecret().isBlank()) {
            formData.add("client_secret", salesforceConfig.getClientSecret());
        }
        formData.add("redirect_uri", salesforceConfig.getRedirectUri());
        if (codeVerifier != null && !codeVerifier.isBlank()) {
            formData.add("code_verifier", codeVerifier);
        }

        try {
            SalesforceToken token = webClient.post()
                    .uri(salesforceConfig.getTokenUrl())
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(SalesforceToken.class)
                    .block();

            if (token == null || token.getAccessToken() == null) {
                throw new AuthenticationException("Failed to obtain access token from Salesforce");
            }

            log.info("Successfully obtained Salesforce access token");
            return token;
        } catch (WebClientResponseException e) {
            log.error("Salesforce token exchange failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthenticationException("Salesforce authentication failed: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            if (e instanceof AuthenticationException) throw e;
            log.error("Error during token exchange: {}", e.getMessage());
            throw new AuthenticationException("Unable to authenticate with Salesforce. Please try again.");
        }
    }

    /**
     * Exchange authorization code for access and refresh tokens.
     */
    public SalesforceToken exchangeCodeForToken(String authorizationCode) {
        return exchangeCodeForToken(authorizationCode, null);
    }

    /**
     * Refresh an expired access token using the refresh token.
     */
    public SalesforceToken refreshToken(String refreshToken) {
        log.debug("Refreshing Salesforce access token");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", refreshToken);
        formData.add("client_id", salesforceConfig.getClientId());
        if (salesforceConfig.getClientSecret() != null && !salesforceConfig.getClientSecret().isBlank()) {
            formData.add("client_secret", salesforceConfig.getClientSecret());
        }

        try {
            SalesforceToken token = webClient.post()
                    .uri(salesforceConfig.getTokenUrl())
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(SalesforceToken.class)
                    .block();

            if (token == null || token.getAccessToken() == null) {
                throw new AuthenticationException("Failed to refresh access token");
            }

            // Salesforce may not return a new refresh token; preserve the existing one
            if (token.getRefreshToken() == null) {
                token.setRefreshToken(refreshToken);
            }

            log.info("Successfully refreshed Salesforce access token");
            return token;
        } catch (WebClientResponseException e) {
            log.error("Salesforce token refresh failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthenticationException("Session expired. Please login again.");
        } catch (Exception e) {
            if (e instanceof AuthenticationException) throw e;
            log.error("Error during token refresh: {}", e.getMessage());
            throw new AuthenticationException("Unable to refresh session. Please login again.");
        }
    }

    /**
     * Get the Salesforce authorization URL for the OAuth flow.
     */
    public String getAuthorizationUrl(String codeChallenge) {
        return salesforceConfig.getAuthorizationUrl(codeChallenge);
    }

    public String getAuthorizationUrl() {
        return salesforceConfig.getAuthorizationUrl();
    }
}
