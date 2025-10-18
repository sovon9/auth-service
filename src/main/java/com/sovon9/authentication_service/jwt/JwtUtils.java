package com.sovon9.authentication_service.jwt;

import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;

//@Configuration
public class JwtUtils {
	
//	@Value("${jwt.jwtSecret}")
//	private String jwtSecret;
//	
//	@Value("${jwt.jwtExpiration}")
//	private Long jwtExpiration;
//	
//	private Key key()
//	{
//		//Creates a new SecretKey instance for use with HMAC-SHA algorithms based on the specified key byte array.
//		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
//	}
//	
//	public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) 
//	{
//		return Jwts.builder()
//                .subject(username)
//                .claim("roles", authorities)
//                .issuedAt(new Date(System.currentTimeMillis()))
//                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
//                .signWith(key())
//                .compact();
//	}
	

}
