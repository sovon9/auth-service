package com.sovon9.authentication_service.mapper;

import com.sovon9.authentication_service.dto.ClientDto;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClientDtoMapper {


    public static ClientDto registeredClientToClientDto(RegisteredClient registeredClient)
    {
        ClientDto dto = new ClientDto(registeredClient.getClientId(), registeredClient.getClientName(),
                registeredClient.getClientIdIssuedAt(), "<PROTECTD>", registeredClient.getAuthorizationGrantTypes().stream().map(rc->rc.getValue()).collect(Collectors.toSet()),
                registeredClient.getClientAuthenticationMethods().stream().map(rc->rc.getValue()).collect(Collectors.toSet()), registeredClient.getRedirectUris(),
                registeredClient.getScopes(), registeredClient.getTokenSettings().getAccessTokenTimeToLive().toMinutes(), registeredClient.getTokenSettings().getRefreshTokenTimeToLive().toHours(), registeredClient.getClientSettings().isRequireProofKey(), registeredClient.getClientSettings().isRequireAuthorizationConsent());
        return dto;
    }


    public static RegisteredClient clientDtoToRegisteredClient(ClientDto dto, RegisteredClient existing)
    {
        // For PUT: use RegisteredClient.from(existing) to preserve the internal UUID id
        // and the existing BCrypt-hashed secret.
        // For POST: use RegisteredClient.withId(UUID.randomUUID().toString())
        RegisteredClient.Builder builder = (existing != null) ? RegisteredClient.from(existing)
                : RegisteredClient.withId(UUID.randomUUID().toString());

        builder
                .clientId(dto.getClientId())
                .clientName(dto.getClientName() != null ? dto.getClientName() : existing.getClientName());
        // NOTE: do NOT set clientIdIssuedAt — it's managed by Spring internally

        // ── Grant types: loop and convert String → AuthorizationGrantType ──
        // First clear existing ones, then add from DTO
        builder.authorizationGrantTypes(grantTypes -> {
            grantTypes.clear();
            dto.getAuthorizationGrantTypes()
                    .forEach(g -> grantTypes.add(new AuthorizationGrantType(g)));
        });

        // ── Auth methods: loop and convert String → ClientAuthenticationMethod ──
        builder.clientAuthenticationMethods(methods -> {
            methods.clear();
            dto.getClientAuthenticationMethods()
                    .forEach(m -> methods.add(new ClientAuthenticationMethod(m)));
        });

        // ── Redirect URIs ──
        builder.redirectUris(uris -> {
            uris.clear();
            if (dto.getRedirectUris() != null) {
                uris.addAll(dto.getRedirectUris());
            }
        });

        // ── Scopes ──
        builder.scopes(scopes -> {
            scopes.clear();
            if (dto.getScopes() != null) {
                scopes.addAll(dto.getScopes());
            }
        });

        // ── Token settings ──
        // For PUT: start from existing settings so internal fields not in the DTO
        // (reuseRefreshTokens, idTokenSignatureAlgorithm, accessTokenFormat, etc.)
        // are preserved. Building from scratch would reset them to defaults and
        // corrupt the DB row.
        // Guard against zero TTL (Java default for long) which would expire tokens instantly.
        TokenSettings.Builder tokenBuilder = (existing != null)
                ? TokenSettings.withSettings(existing.getTokenSettings().getSettings())
                : TokenSettings.builder();
        if (dto.getAccessTokenTtlMinutes() > 0) {
            tokenBuilder.accessTokenTimeToLive(Duration.ofMinutes(dto.getAccessTokenTtlMinutes()));
        }
        if (dto.getRefreshTokenTtlHours() > 0) {
            tokenBuilder.refreshTokenTimeToLive(Duration.ofHours(dto.getRefreshTokenTtlHours()));
        }
        builder.tokenSettings(tokenBuilder.build());

        // ── Client settings ──
        // Same principle: start from existing to preserve any fields not in the DTO.
        ClientSettings.Builder clientSettingsBuilder = (existing != null)
                ? ClientSettings.withSettings(existing.getClientSettings().getSettings())
                : ClientSettings.builder();
        clientSettingsBuilder
                .requireProofKey(dto.isRequireProofKey())
                .requireAuthorizationConsent(dto.isRequireAuthorizationConsent());
        builder.clientSettings(clientSettingsBuilder.build());

            // ── Secret: only update if a new one was provided in the DTO ──
            // (Password encoding happens in the SERVICE, not the mapper)
            if (dto.getClientSecret() != null && !dto.getClientSecret().isBlank()) {
                builder.clientSecret(dto.getClientSecret()); // already encoded by service
            }

        return builder.build();
    }

}
