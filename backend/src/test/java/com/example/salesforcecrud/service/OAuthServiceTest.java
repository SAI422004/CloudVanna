package com.example.salesforcecrud.service;

import com.example.salesforcecrud.config.SalesforceConfig;
import com.example.salesforcecrud.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OAuthServiceTest {

    private OAuthService oAuthService;
    private SalesforceConfig salesforceConfig;

    @BeforeEach
    void setUp() {
        salesforceConfig = new SalesforceConfig();
        salesforceConfig.setClientId("test-client-id");
        salesforceConfig.setClientSecret("test-secret");
        salesforceConfig.setRedirectUri("http://localhost:8080/api/auth/callback");
        salesforceConfig.setLoginUrl("https://login.salesforce.com");

        oAuthService = new OAuthService(salesforceConfig);
    }

    @Test
    void getAuthorizationUrlShouldContainRequiredParams() {
        String url = oAuthService.getAuthorizationUrl();

        assertNotNull(url);
        assertTrue(url.contains("https://login.salesforce.com/services/oauth2/authorize"));
        assertTrue(url.contains("response_type=code"));
        assertTrue(url.contains("client_id=test-client-id"));
        assertTrue(url.contains("redirect_uri="));
    }

    @Test
    void exchangeCodeForTokenShouldThrowOnNull() {
        // WebClient call to non-existent endpoint should fail gracefully
        assertThrows(AuthenticationException.class,
                () -> oAuthService.exchangeCodeForToken(null));
    }

    @Test
    void refreshTokenShouldThrowOnNull() {
        assertThrows(AuthenticationException.class,
                () -> oAuthService.refreshToken(null));
    }

    @Test
    void configShouldBuildCorrectTokenUrl() {
        assertEquals("https://login.salesforce.com/services/oauth2/token",
                salesforceConfig.getTokenUrl());
    }

    @Test
    void configShouldBuildCorrectAuthorizationUrl() {
        String authUrl = salesforceConfig.getAuthorizationUrl();
        assertTrue(authUrl.startsWith("https://login.salesforce.com/services/oauth2/authorize"));
        assertTrue(authUrl.contains("response_type=code"));
        assertTrue(authUrl.contains("client_id=test-client-id"));
    }
}
