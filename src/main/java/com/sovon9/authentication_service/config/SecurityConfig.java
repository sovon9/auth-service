package com.sovon9.authentication_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
	public SecurityFilterChain authorizationServerChain(HttpSecurity http) throws Exception
	{
		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
		return http.build();
	}
	
	@Bean
	public SecurityFilterChain filter(HttpSecurity http) throws Exception
	{
		http.authorizeHttpRequests(request->
		request.anyRequest().authenticated())
		.securityMatcher("/api/**")
		.oauth2ResourceServer(o->o.jwt(Customizer.withDefaults()));
		http.csrf(csrf->csrf.disable());
		return http.build();
	}
	
//	@Bean
//	public AuthenticationProvider authprovider()
//	{
//		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//		provider.setUserDetailsService(null);
//		provider.setPasswordEncoder(null);
//		return provider;
//	}
	
}
