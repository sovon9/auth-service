package com.sovon9.authentication_service.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Configuration
public class SecurityConfig {

//	@Bean
//	@Order(1)
//	public SecurityFilterChain authorizationServerChain(HttpSecurity http) throws Exception
//	{
//
////		 This single line applies all standard protocol matchers and endpoints cleanly
//        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
//
//        // Enable OpenID Connect (OIDC) 1.0
//        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
//                .oidc(Customizer.withDefaults());
//
//        // Ensure unauthenticated users are sent to /login instead of falling through to /error
//        http.exceptionHandling(exceptions -> exceptions
//                .defaultAuthenticationEntryPointFor(
//                        new LoginUrlAuthenticationEntryPoint("/login"),
//                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
//                )
//        );
//
//        return http.build();
//	}

@Bean
@Order(1)
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
		throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
			.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer, (authorizationServer) ->
					authorizationServer
							.oidc(Customizer.withDefaults())	// Enable OpenID Connect 1.0
			)
			.authorizeHttpRequests((authorize) ->
					authorize
							.anyRequest().authenticated()
			)
			// Redirect to the login page when not authenticated from the
			// authorization endpoint
			.exceptionHandling((exceptions) -> exceptions
					.defaultAuthenticationEntryPointFor(
							new LoginUrlAuthenticationEntryPoint("/login"),
							new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
					)
			);

	return http.build();
}

	@Bean
	@Order(2)
	public SecurityFilterChain filter(HttpSecurity http) throws Exception
	{
		http.authorizeHttpRequests(request -> request
				.requestMatchers("/actuator/health", "/error").permitAll()
				// /admin/** requires a valid JWT with scope=admin.write
				// Spring auto-prefixes scope values with SCOPE_ in the authority list
				.requestMatchers("/admin/**").hasAuthority("SCOPE_admin.write")
				.anyRequest().authenticated())
				.formLogin(Customizer.withDefaults())
				// Validates Bearer JWT tokens issued by this authorization server
				.oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()));
		return http.build();
	}

	// UserDetailsService is provided by MyUserDetailsService (@Service)
	// Spring auto-detects and wires it — no bean definition needed here.

	/**
	 * Adds roles claim to jwt token
	 * @return
	 */
	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
		return context -> {
			// 1. Only customize the Access Token (ignore refresh tokens or ID tokens for now)
			if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {

				// 2. Grab the human user's authentication object
				Authentication principal = context.getPrincipal();

				// 3. Extract all authorities (e.g., ROLE_OPERATOR, ROLE_USER)
				Set<String> authorities = principal.getAuthorities().stream()
						.map(GrantedAuthority::getAuthority)
						.collect(Collectors.toSet());

				// 4. Inject them into the JWT payload under a custom claim named "roles"
				context.getClaims().claim("roles", authorities);
			}
		};
	}

	@Bean
	PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}

	@Bean
	public RegisteredClientRepository registeredClientRepository(PasswordEncoder encoder, JdbcTemplate jdbcTemplate)
	{
		return new JdbcRegisteredClientRepository(jdbcTemplate);
	}

	@Bean
	OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository repo)
	{
		return new JdbcOAuth2AuthorizationService(jdbcTemplate, repo);
	}

	@Bean
	OAuth2AuthorizationConsentService auth2AuthorizationConsentService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository)
	{
		return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
	}

	// Json Web key
	@Bean
	JWKSource<SecurityContext> jwkSource() throws Exception
	{
		KeyPair kp = generateRsaKey();
		RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey)kp.getPublic())
				.privateKey(kp.getPrivate())
				.keyID(UUID.randomUUID().toString())
				.build();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return (jwkSelector, context) -> jwkSelector.select(jwkSet);
	}

	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource)
	{
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	@Bean
	public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource)
	{
		return new NimbusJwtEncoder(jwkSource);
	}

	private KeyPair generateRsaKey() throws NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		return kpg.generateKeyPair();
	}

}
