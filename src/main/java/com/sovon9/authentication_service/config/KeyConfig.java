package com.sovon9.authentication_service.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class KeyConfig {

	@Bean
	public JWKSource<SecurityContext> jwkSource() throws NoSuchAlgorithmException
	{
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();
		RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey)kp.getPublic())
				.privateKey((RSAPrivateKey)kp.getPrivate())
				.keyID("demo-key-1")
				.build();
		JWKSet jwtSet = new JWKSet(rsaKey);
		return (selector, ctx) -> selector.select(jwtSet);
	}
	
}
