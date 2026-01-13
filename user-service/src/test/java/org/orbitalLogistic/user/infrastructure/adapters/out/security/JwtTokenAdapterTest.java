package org.orbitalLogistic.user.infrastructure.adapters.out.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.domain.model.User;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenAdapter Tests")
class JwtTokenAdapterTest {

    private JwtTokenAdapter jwtTokenAdapter;
    private String secretKey;
    private long jwtExpiration;

    @BeforeEach
    void setUp() {
        jwtTokenAdapter = new JwtTokenAdapter();
        secretKey = "mySecretKeyForTestingPurposesOnlyItMustBeLongEnough123456789";
        jwtExpiration = 3600L; // 1 hour in seconds

        ReflectionTestUtils.setField(jwtTokenAdapter, "secret", secretKey);
        ReflectionTestUtils.setField(jwtTokenAdapter, "expiration", jwtExpiration);
    }

    private User createTestUser(String username, String... roleNames) {
        Set<Role> roles = new java.util.HashSet<>();
        for (String roleName : roleNames) {
            roles.add(Role.builder().name(roleName).build());
        }
        return User.builder()
                .username(username)
                .password("password")
                .email(username + "@test.com")
                .enabled(true)
                .roles(roles)
                .build();
    }

    @Test
    @DisplayName("Should generate token")
    void shouldGenerateToken() {
        // Given
        User user = createTestUser("testuser", "ADMIN", "USER");

        // When
        String token = jwtTokenAdapter.generateToken(user);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsername() {
        // Given
        User user = createTestUser("testuser", "ADMIN");
        String token = jwtTokenAdapter.generateToken(user);

        // When
        String extractedUsername = jwtTokenAdapter.extractUsername(token);

        // Then
        assertEquals("testuser", extractedUsername);
    }

    @Test
    @DisplayName("Should extract roles from token")
    void shouldExtractRoles() {
        // Given
        User user = createTestUser("testuser", "ADMIN", "USER");
        String token = jwtTokenAdapter.generateToken(user);

        // When
        java.util.List<String> extractedRoles = jwtTokenAdapter.extractRoles(token);

        // Then
        assertNotNull(extractedRoles);
        assertEquals(2, extractedRoles.size());
        assertTrue(extractedRoles.contains("ADMIN"));
        assertTrue(extractedRoles.contains("USER"));
    }

    @Test
    @DisplayName("Should validate token successfully")
    void shouldValidateTokenSuccessfully() {
        // Given
        User user = createTestUser("testuser", "ADMIN");
        String token = jwtTokenAdapter.generateToken(user);

        // When
        boolean isValid = jwtTokenAdapter.validateToken(token, user);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should invalidate token with wrong username")
    void shouldInvalidateTokenWithWrongUsername() {
        // Given
        User user = createTestUser("testuser", "ADMIN");
        User wrongUser = createTestUser("wronguser", "ADMIN");
        String token = jwtTokenAdapter.generateToken(user);

        // When
        boolean isValid = jwtTokenAdapter.validateToken(token, wrongUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should detect expired token")
    void shouldDetectExpiredToken() {
        // Given - создаем токен с истекшим временем
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        String expiredToken = Jwts.builder()
                .subject("testuser")
                .claim("roles", java.util.Arrays.asList("ADMIN"))
                .issuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .expiration(new Date(System.currentTimeMillis() - 3600000)) // expired 1 hour ago
                .signWith(key)
                .compact();

        // When & Then
        // Token истек, поэтому любая валидация должна вернуть false
        User user = createTestUser("testuser", "ADMIN");
        assertFalse(jwtTokenAdapter.validateToken(expiredToken, user));
    }

    @Test
    @DisplayName("Should detect non-expired token")
    void shouldDetectNonExpiredToken() {
        // Given
        User user = createTestUser("testuser", "ADMIN");
        String token = jwtTokenAdapter.generateToken(user);

        // When
        boolean isValid = jwtTokenAdapter.validateToken(token, user);

        // Then
        assertTrue(isValid);
    }
}
