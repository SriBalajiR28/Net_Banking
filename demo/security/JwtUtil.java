//sribalajir90@gmail.com&bsri61928@gmail.com
package com.cmrit.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private long expirationTime;

	// ✅ Key built fresh each time using the injected secret
	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	// ✅ Generate token with username + roles embedded
	public String generateToken(String username, List<String> roles) {
		return Jwts.builder().setSubject(username).claim("roles", roles).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + expirationTime))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
	}

	// ✅ Extract username from token
	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	// ✅ Extract roles list from token claims
	public List<String> extractRoles(String token) {
		return extractAllClaims(token).get("roles", List.class);
	}

	// ✅ Validate token against loaded UserDetails
	public boolean validateToken(String token, UserDetails userDetails) {
		try {
			String extractedUsername = extractUsername(token);
			return extractedUsername.equals(userDetails.getUsername()) && !isTokenExpired(token);
		} catch (JwtException e) {
			return false;
		}
	}

	private boolean isTokenExpired(String token) {
		return extractAllClaims(token).getExpiration().before(new Date());
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
	}
}