package de.greenflash.taskserviceapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import de.greenflash.taskserviceapi.config.JwtProperties;
import java.time.Instant;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtService {

    // Generates and validates JWTs using a shared HMAC secret.
    private final JwtProperties jwtProperties;

    /**
     * Create a signed JWT for the given username.
     */
    public String generateToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.getExpirationMs())))
                .signWith(signingKey())
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
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Create an HMAC signing key from the configured secret.
    private Key signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
