package com.redesocial.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

	private String jwtSecret =
			"GV9nLNTUwe5rXFq3aKqe7VKn0oYee9kuN0ZRZPJltO9PaxnsQRpLf41hMzZmB2qu5B9fQBMAEe647PaXXloMyxHZMDqmscTLUquh";

	private Long jwtExpirationMiliseg = 90000000L;

	public String generateToken(String username) {
		SecretKey secreKeySpec = Keys.hmacShaKeyFor(jwtSecret.getBytes());
		return Jwts.builder().setSubject(username)
				.setExpiration(new Date(System.currentTimeMillis() + this.jwtExpirationMiliseg)).signWith(secreKeySpec)
				.compact();
	}

	public boolean isValidToken(String token) {
		Claims claims = getClaims(token);
		if (claims != null) {
			String username = claims.getSubject();
			Date expirationDate = claims.getExpiration();
			Date now = new Date(System.currentTimeMillis());
			if (username != null && expirationDate != null && now.before(expirationDate)) {
				return true;
			}
		}
		return false;
	}

	public String getUserName(String token) {
		Claims claims = getClaims(token);
		if (claims != null) {
			return claims.getSubject();
		}
		return null;
	}

	public Claims getClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(jwtSecret.getBytes()).build().parseClaimsJws(token).getBody();
	}


}
