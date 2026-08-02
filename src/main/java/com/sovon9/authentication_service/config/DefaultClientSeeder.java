package com.sovon9.authentication_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

/**
 * Seeds default OAuth2 clients into the DB on first startup.
 * Idempotent — checks by clientId before inserting, safe across restarts.
 *
 * @Order(2) ensures this runs AFTER DefaultUserSeeder (@Order(1) by default).
 */
@Component
@Order(2)
public class DefaultClientSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultClientSeeder.class);

    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultClientSeeder(RegisteredClientRepository registeredClientRepository,
                               PasswordEncoder passwordEncoder) {
        this.registeredClientRepository = registeredClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedClientCredentialsClient();
        seedAuthorizationCodeClient();
        seedAdminClient();
    }

    /**
     * Machine-to-machine client using client_credentials grant.
     * Suitable for backend services calling each other without a user context.
     */
    private void seedClientCredentialsClient() {
        String clientId = "demo-client";

        if (registeredClientRepository.findByClientId(clientId) != null) {
            log.info("OAuth2 client '{}' already exists — skipping seed.", clientId);
            return;
        }

        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode("demo-secret"))
                .clientName("Demo Machine Client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("demo.read")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(10))
                        .build())
                .build();

        registeredClientRepository.save(client);
        log.info("OAuth2 client '{}' seeded successfully.", clientId);
    }

    /**
     * Browser/user-facing client using authorization_code grant.
     * Suitable for web apps where a real user logs in.
     */
    private void seedAuthorizationCodeClient() {
        String clientId = "demo-app";

        if (registeredClientRepository.findByClientId(clientId) != null) {
            log.info("OAuth2 client '{}' already exists — skipping seed.", clientId);
            return;
        }

        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode("demo-app-secret"))
                .clientName("Demo Web App")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
//                .redirectUri("http://127.0.0.1:9000/login/authorized")
                .redirectUri("http://localhost:8081/login/oauth2/code/demo-app")
                .scopes(scp -> {
                    scp.add("demo.read");
                    scp.add("openid");
                })
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)             // set true to enforce PKCE in prod
                        .requireAuthorizationConsent(false) // set true to show a consent screen
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(10))
                        .refreshTokenTimeToLive(Duration.ofHours(4))
                        .build())
                .build();

        registeredClientRepository.save(client);
        log.info("OAuth2 client '{}' seeded successfully.", clientId);
    }

    /**
     * Admin client using client_credentials grant with scope admin.write.
     * Only this client can obtain tokens that grant access to /admin/** endpoints.
     * The client secret acts as the admin credential — keep it secret in production.
     */
    private void seedAdminClient() {
        String clientId = "admin-client";

        if (registeredClientRepository.findByClientId(clientId) != null) {
            log.info("OAuth2 client '{}' already exists — skipping seed.", clientId);
            return;
        }

        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode("admin-secret"))
                .clientName("Admin Management Client")
                // CLIENT_SECRET_BASIC: pass credentials as Basic Auth header in /oauth2/token call
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                // admin.write is the gate — only tokens with this scope can hit /admin/**
                .scope("admin.write")
                .scope(OidcScopes.OPENID)
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .build())
                .build();

        registeredClientRepository.save(client);
        log.info("OAuth2 admin client '{}' seeded successfully.", clientId);
    }
}
