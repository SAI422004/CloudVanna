package com.example.salesforcecrud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds Salesforce OAuth configuration from environment variables.
 */
@Configuration
@ConfigurationProperties(prefix = "salesforce")
public class SalesforceConfig {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String loginUrl;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    /**
     * Build the Salesforce authorization URL for OAuth 2.0 with PKCE.
     */
    public String getAuthorizationUrl(String codeChallenge) {
        String encodedRedirectUri = java.net.URLEncoder.encode(redirectUri, java.nio.charset.StandardCharsets.UTF_8);
        String url = loginUrl + "/services/oauth2/authorize"
                + "?response_type=code"
                + "&client_id=" + java.net.URLEncoder.encode(clientId, java.nio.charset.StandardCharsets.UTF_8)
                + "&redirect_uri=" + encodedRedirectUri;
        if (codeChallenge != null && !codeChallenge.isBlank()) {
            url += "&code_challenge=" + java.net.URLEncoder.encode(codeChallenge, java.nio.charset.StandardCharsets.UTF_8) + "&code_challenge_method=S256";
        }
        return url;
    }

    /**
     * Build the Salesforce authorization URL for OAuth 2.0.
     */
    public String getAuthorizationUrl() {
        return getAuthorizationUrl(null);
    }

    /**
     * Build the Salesforce token endpoint URL.
     */
    public String getTokenUrl() {
        return loginUrl + "/services/oauth2/token";
    }
}
