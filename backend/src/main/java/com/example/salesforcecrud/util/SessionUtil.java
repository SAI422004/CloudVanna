package com.example.salesforcecrud.util;

import com.example.salesforcecrud.exception.AuthenticationException;
import com.example.salesforcecrud.model.SalesforceToken;
import jakarta.servlet.http.HttpSession;

/**
 * Utility for retrieving the Salesforce token from the HTTP session.
 */
public final class SessionUtil {

    public static final String SESSION_TOKEN_KEY = "salesforce_token";

    private SessionUtil() {}

    /**
     * Get the Salesforce token from session, or throw if not authenticated.
     */
    public static SalesforceToken getToken(HttpSession session) {
        SalesforceToken token = (SalesforceToken) session.getAttribute(SESSION_TOKEN_KEY);
        if (token == null || token.getAccessToken() == null) {
            throw new AuthenticationException("Not authenticated. Please login with Salesforce.");
        }
        return token;
    }
}
