package com.sovon9.authentication_service.dto;

import java.time.Instant;
import java.util.Set;

public class ClientDto {

        // --- Identity (read-only on GET, not changeable via PUT) ---
        private String clientId;
        private String clientName;
        private Instant clientIdIssuedAt;        // when the client was created

        // --- Secret ---
        // On GET  → never return the actual secret (security risk), return null or "****"
        // On PUT  → if sent, re-encode and update; if null/blank, keep existing secret
        private String clientSecret;

        // --- Grant types & auth methods ---
        private Set<String> authorizationGrantTypes;    // "authorization_code", "client_credentials", "refresh_token"
        private Set<String> clientAuthenticationMethods; // "client_secret_basic", "client_secret_post"

        // --- The dynamic parts admins want to change ---
        private Set<String> redirectUris;
        private Set<String> scopes;

        // --- Token settings ---
        private long accessTokenTtlMinutes;
        private long refreshTokenTtlHours;

        // --- Client settings ---
        private boolean requireProofKey;
        private boolean requireAuthorizationConsent;

        public ClientDto(String clientId, String clientName, Instant clientIdIssuedAt, String clientSecret, Set<String> authorizationGrantTypes, Set<String> clientAuthenticationMethods, Set<String> redirectUris, Set<String> scopes, long accessTokenTtlMinutes, long refreshTokenTtlHours, boolean requireProofKey, boolean requireAuthorizationConsent) {
                this.clientId = clientId;
                this.clientName = clientName;
                this.clientIdIssuedAt = clientIdIssuedAt;
                this.clientSecret = clientSecret;
                this.authorizationGrantTypes = authorizationGrantTypes;
                this.clientAuthenticationMethods = clientAuthenticationMethods;
                this.redirectUris = redirectUris;
                this.scopes = scopes;
                this.accessTokenTtlMinutes = accessTokenTtlMinutes;
                this.refreshTokenTtlHours = refreshTokenTtlHours;
                this.requireProofKey = requireProofKey;
                this.requireAuthorizationConsent = requireAuthorizationConsent;
        }

        public ClientDto() {
        }

        public String getClientId() {
                return clientId;
        }

        public void setClientId(String clientId) {
                this.clientId = clientId;
        }

        public String getClientName() {
                return clientName;
        }

        public void setClientName(String clientName) {
                this.clientName = clientName;
        }

        public Instant getClientIdIssuedAt() {
                return clientIdIssuedAt;
        }

        public void setClientIdIssuedAt(Instant clientIdIssuedAt) {
                this.clientIdIssuedAt = clientIdIssuedAt;
        }

        public String getClientSecret() {
                return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
        }

        public Set<String> getAuthorizationGrantTypes() {
                return authorizationGrantTypes;
        }

        public void setAuthorizationGrantTypes(Set<String> authorizationGrantTypes) {
                this.authorizationGrantTypes = authorizationGrantTypes;
        }

        public Set<String> getClientAuthenticationMethods() {
                return clientAuthenticationMethods;
        }

        public void setClientAuthenticationMethods(Set<String> clientAuthenticationMethods) {
                this.clientAuthenticationMethods = clientAuthenticationMethods;
        }

        public Set<String> getRedirectUris() {
                return redirectUris;
        }

        public void setRedirectUris(Set<String> redirectUris) {
                this.redirectUris = redirectUris;
        }

        public Set<String> getScopes() {
                return scopes;
        }

        public void setScopes(Set<String> scopes) {
                this.scopes = scopes;
        }

        public long getAccessTokenTtlMinutes() {
                return accessTokenTtlMinutes;
        }

        public void setAccessTokenTtlMinutes(long accessTokenTtlMinutes) {
                this.accessTokenTtlMinutes = accessTokenTtlMinutes;
        }

        public long getRefreshTokenTtlHours() {
                return refreshTokenTtlHours;
        }

        public void setRefreshTokenTtlHours(long refreshTokenTtlHours) {
                this.refreshTokenTtlHours = refreshTokenTtlHours;
        }

        public boolean isRequireProofKey() {
                return requireProofKey;
        }

        public void setRequireProofKey(boolean requireProofKey) {
                this.requireProofKey = requireProofKey;
        }

        public boolean isRequireAuthorizationConsent() {
                return requireAuthorizationConsent;
        }

        public void setRequireAuthorizationConsent(boolean requireAuthorizationConsent) {
                this.requireAuthorizationConsent = requireAuthorizationConsent;
        }
}