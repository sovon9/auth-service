package com.sovon9.authentication_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {
	
	@Bean
	public SecurityFilterChain authorizationServerChain(HttpSecurity http) throws Exception
	{
		OAuth2AuthorizationServerConfigurer serverConfigurer = new OAuth2AuthorizationServerConfigurer();
		http
		.securityMatcher(serverConfigurer.getEndpointsMatcher())
		.authorizeHttpRequests(a->a.anyRequest().authenticated())
		.csrf(csrf->csrf.disable())
		.apply(serverConfigurer);
		
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
	
	
}
