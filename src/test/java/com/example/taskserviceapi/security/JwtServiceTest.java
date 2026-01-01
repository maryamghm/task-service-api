package com.example.taskserviceapi.security;

import static com.example.taskserviceapi.TestFixtures.JWT_SECRET;
import static com.example.taskserviceapi.TestFixtures.USER_A_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.taskserviceapi.config.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    @Test
    void generateTokenAndExtractUsername() {
        JwtService jwtService = jwtService(3600000);

        String token = jwtService.generateToken(USER_A_USERNAME);

        assertThat(jwtService.extractUsername(token)).isEqualTo(USER_A_USERNAME);
        assertThat(jwtService.isTokenValid(token, USER_A_USERNAME)).isTrue();
    }

    @Test
    void tokenValidationFailsForDifferentUser() {
        JwtService jwtService = jwtService(3600000);

        String token = jwtService.generateToken(USER_A_USERNAME);

        assertThat(jwtService.isTokenValid(token, "someone-else")).isFalse();
    }

    @Test
    void expiredTokenIsInvalid() {
        JwtService jwtService = jwtService(-1000);

        String token = jwtService.generateToken(USER_A_USERNAME);

        assertThatThrownBy(() -> jwtService.isTokenValid(token, USER_A_USERNAME))
                .isInstanceOf(ExpiredJwtException.class);
    }

    private JwtProperties jwtProperties(long expirationMs) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(JWT_SECRET);
        properties.setExpirationMs(expirationMs);
        return properties;
    }

    private JwtService jwtService(long expirationMs) {
        return new JwtService(
                jwtProperties(expirationMs),
                Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)));
    }
}
