package com.example.salesforcecrud.controller;

import com.example.salesforcecrud.model.SalesforceToken;
import com.example.salesforcecrud.service.OAuthService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * Handles Salesforce OAuth 2.0 authentication flow.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final String SESSION_TOKEN_KEY = "salesforce_token";
    private static final String SESSION_CODE_VERIFIER_KEY = "oauth_code_verifier";

    private final OAuthService oAuthService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public AuthController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    private String getRedirectFrontendUrl() {
        if (frontendUrl == null || frontendUrl.isBlank()) {
            return "http://localhost:5173";
        }
        String target = frontendUrl.split(",")[0].trim();
        if (target.endsWith("/")) {
            target = target.substring(0, target.length() - 1);
        }
        return target;
    }

    /**
     * Redirect the user to Salesforce's authorization page.
     */
    @GetMapping("/login")
    public void login(HttpSession session, jakarta.servlet.http.HttpServletResponse response) throws IOException {
        String codeVerifier = OAuthService.generateCodeVerifier();
        String codeChallenge = OAuthService.generateCodeChallenge(codeVerifier);
        session.setAttribute(SESSION_CODE_VERIFIER_KEY, codeVerifier);

        String authUrl = oAuthService.getAuthorizationUrl(codeChallenge);
        log.debug("Redirecting to Salesforce authorization URL with PKCE");
        response.sendRedirect(authUrl);
    }

    /**
     * Handle the OAuth callback from Salesforce.
     * Exchange the authorization code for tokens and store in session.
     */
    @GetMapping("/callback")
    public void callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription,
            HttpSession session,
            jakarta.servlet.http.HttpServletResponse response) throws IOException {

        String redirectBase = getRedirectFrontendUrl();

        // Handle user rejection or Salesforce errors
        if (error != null) {
            log.warn("Salesforce OAuth error: {} - {}", error, errorDescription);
            response.sendRedirect(redirectBase + "?auth_error=" + error);
            return;
        }

        if (code == null || code.isBlank()) {
            log.warn("No authorization code received in callback");
            response.sendRedirect(redirectBase + "?auth_error=no_code");
            return;
        }

        String codeVerifier = (String) session.getAttribute(SESSION_CODE_VERIFIER_KEY);
        session.removeAttribute(SESSION_CODE_VERIFIER_KEY);

        try {
            SalesforceToken token = oAuthService.exchangeCodeForToken(code, codeVerifier);
            session.setAttribute(SESSION_TOKEN_KEY, token);
            log.info("User authenticated successfully, redirecting to frontend");
            response.sendRedirect(redirectBase + "?auth_success=true");
        } catch (Exception e) {
            log.error("OAuth callback failed: {}", e.getMessage());
            response.sendRedirect(redirectBase + "?auth_error=token_exchange_failed");
        }
    }

    /**
     * Log the user out by invalidating the session.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        session.invalidate();
        log.info("User logged out");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Check if the user is currently authenticated.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(HttpSession session) {
        SalesforceToken token = (SalesforceToken) session.getAttribute(SESSION_TOKEN_KEY);
        boolean authenticated = token != null && token.getAccessToken() != null;
        return ResponseEntity.ok(Map.of("authenticated", authenticated));
    }
}
