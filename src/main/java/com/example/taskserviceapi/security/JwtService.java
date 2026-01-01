package com.example.taskserviceapi.security;

import com.example.taskserviceapi.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtService {

    // Generates and validates JWTs using a shared HMAC secret.
    private final JwtProperties jwtProperties;

    // Create an HMAC signing key from the configured secret.
    private final SecretKey signingKey;

    /**
     * Create a signed JWT for the given username.
     */
    public String generateToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.getExpirationMs())))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extract the username from a valid JWT.
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Validate signature, expiration, and subject.
     */
    public boolean isTokenValid(String token, String username) {
        Claims claims = parseClaims(token);
        return username.equals(claims.getSubject()) && !claims.getExpiration().before(new Date());
    }

    // Parse and verify a signed JWT.
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
