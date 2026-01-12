package org.orbitalLogistic.inventory.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private String secretKey;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        signingKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        secretKey = Base64.getEncoder().encodeToString(signingKey.getEncoded());
        
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
    }

    @Test
    @DisplayName("Should validate valid token")
    void validateToken_Valid() {
        String token = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .signWith(signingKey)
                .compact();
        boolean result = jwtService.validateToken(token);
        assertTrue(result);
    }

    @Test
    @DisplayName("Should invalidate expired token")
    void validateToken_Expired() {
        String token = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 120)) // 2 hours ago
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // 1 hour ago
                .signWith(signingKey)
                .compact();
        boolean result = jwtService.validateToken(token);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should invalidate token with invalid signature")
    void validateToken_InvalidSignature() {
        SecretKey differentKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String token = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(differentKey)
                .compact();
        boolean result = jwtService.validateToken(token);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should invalidate malformed token")
    void validateToken_Malformed() {
        String token = "malformed.token.string";
        boolean result = jwtService.validateToken(token);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should extract all claims from valid token")
    void extractAllClaims_Success() {
        String username = "testuser";
        String token = Jwts.builder()
                .setSubject(username)
                .claim("userId", 123L)
                .claim("email", "test@example.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(signingKey)
                .compact();
        Claims claims = jwtService.extractAllClaims(token);
        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertEquals(123, claims.get("userId", Integer.class));
        assertEquals("test@example.com", claims.get("email", String.class));
    }

    @Test
    @DisplayName("Should throw exception when extracting claims from invalid token")
    void extractAllClaims_InvalidToken() {
        String token = "invalid.token.string";
        assertThrows(JwtException.class, () -> jwtService.extractAllClaims(token));
    }

    @Test
    @DisplayName("Should handle token without expiration")
    void validateToken_NoExpiration() {
        String token = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .signWith(signingKey)
                .compact();
        boolean result = jwtService.validateToken(token);
        assertFalse(result); // Should fail because no expiration
    }

    @Test
    @DisplayName("Should handle empty token")
    void validateToken_EmptyToken() {
        boolean result = jwtService.validateToken("");
        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle null token")
    void validateToken_NullToken() {
        boolean result = jwtService.validateToken(null);
        assertFalse(result);
    }
}
