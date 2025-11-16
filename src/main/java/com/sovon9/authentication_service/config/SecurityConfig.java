package com.sovon9.authentication_service.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;


@Configuration
public class SecurityConfig {
	
	@Bean
	public SecurityFilterChain authorizationServerChain(HttpSecurity http) throws Exception
	{
//		OAuth2AuthorizationServerConfigurer serverConfigurer = new OAuth2AuthorizationServerConfigurer();
//		http
//		.securityMatcher(serverConfigurer.getEndpointsMatcher())
//		.authorizeHttpRequests(a->a.anyRequest().authenticated())
//		.csrf(csrf->csrf.disable())
//		.apply(serverConfigurer);
		
		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
		
		return http.build();
	}
	
	@Bean
	public SecurityFilterChain filter(HttpSecurity http) throws Exception
	{
		http.authorizeHttpRequests(request->
		request.requestMatchers("/actuator/health").permitAll()
		.anyRequest().authenticated())
		.formLogin(Customizer.withDefaults());
		//.oauth2ResourceServer(o->o.jwt(Customizer.withDefaults()))
		return http.build();
	}
	
	@Bean
	public UserDetailsService userDetailsService(PasswordEncoder encoder)
	{
		UserDetails userDetails = User.withUsername("user1").password(encoder.encode("password"))
				.roles("User").build();
		return new InMemoryUserDetailsManager(userDetails);
	}
	
	@Bean
	PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public RegisteredClientRepository registeredClientRepository(PasswordEncoder encoder)
	{
		RegisteredClient clientCreds = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("demo-client")
				.clientSecret(encoder.encode("demo-secret"))
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.scope("demo.read")
				.tokenSettings(TokenSettings.builder()
						.accessTokenTimeToLive(Duration.ofMinutes(10))
						.build())
				.build();
		
		RegisteredClient authCode = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("demo-app")
				.clientSecret(encoder.encode("demo-app-secret"))
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.redirectUri("http://127.0.0.1:9000/login/oauth2/code/demo-app")
				.scope(OidcScopes.OPENID)
				.scope("demo-read")
				.clientSettings(ClientSettings.builder().requireProofKey(true).build())
				.tokenSettings(TokenSettings.builder()
						.accessTokenTimeToLive(Duration.ofMinutes(10))
						.refreshTokenTimeToLive(Duration.ofHours(4))
						.build())
				.build();
		
		return new InMemoryRegisteredClientRepository(clientCreds, authCode);
	}
	
	@Bean
	OAuth2AuthorizationService authorizationService(RegisteredClientRepository repo)
	{
		return new InMemoryOAuth2AuthorizationService();
	}
	
	@Bean
	OAuth2AuthorizationConsentService auth2AuthorizationConsentService()
	{
		return new InMemoryOAuth2AuthorizationConsentService();
	}
	
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
