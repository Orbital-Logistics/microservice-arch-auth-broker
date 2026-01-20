package org.orbitalLogistic.file.adapters.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class JwtServiceTest {

    private JwtService jwtService;
    private String secretKey;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        secretKey = "dGhpc2lzYXZlcnlzZWNyZXRrZXl0aGF0aXNsb25nZW5vdWdoZm9ySFMyNTY=";
        ReflectionTestUtils.setField(jwtService, "jwtSecret", secretKey);

        byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    void shouldExtractUsernameFromValidToken() {
        String username = "testuser";
        String token = createToken(username, 1L, List.of("USER"), 3600000L);

        
        String extractedUsername = jwtService.getUsernameFromToken(token);

        
        assertEquals(username, extractedUsername);
    }

    @Test
    void shouldExtractSubjectFromValidToken() {
        String subject = "testuser";
        String token = createToken(subject, 1L, List.of("USER"), 3600000L);

        
        String extractedSubject = jwtService.getSubjectFromToken(token);

        
        assertEquals(subject, extractedSubject);
    }

    @Test
    void shouldExtractRolesFromValidToken() {
        List<String> roles = List.of("USER", "ADMIN");
        String token = createToken("testuser", 1L, roles, 3600000L);

        
        List<?> extractedRoles = jwtService.getRolesFromToken(token);

        
        assertNotNull(extractedRoles);
        assertEquals(2, extractedRoles.size());
        assertTrue(extractedRoles.contains("USER"));
        assertTrue(extractedRoles.contains("ADMIN"));
    }

    @Test
    void shouldExtractUserIdFromValidToken() {
        Long userId = 123L;
        String token = createToken("testuser", userId, List.of("USER"), 3600000L);

        
        Long extractedUserId = jwtService.getUserIdFromToken(token);

        
        assertEquals(userId, extractedUserId);
    }

    @Test
    void shouldExtractClaimsFromExpiredToken() {
        String token = createToken("testuser", 1L, List.of("USER"), -1000L);

        
        String username = jwtService.getUsernameFromToken(token);
        assertEquals("testuser", username);
    }

    @Test
    void shouldThrowExceptionForInvalidTokenSignature() {
        SecretKey differentKey = Keys.hmacShaKeyFor("anothersecretkeythatisalsoverylongforHS256bits".getBytes());
        String token = Jwts.builder()
                .subject("testuser")
                .claim("username", "testuser")
                .claim("userId", 1L)
                .claim("roles", List.of("USER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(differentKey)
                .compact();

        
        assertThrows(Exception.class, () -> jwtService.getUsernameFromToken(token));
    }

    @Test
    void shouldGenerateValidServiceToken() {
        
        String serviceToken = jwtService.generateServiceToken();

        
        assertNotNull(serviceToken);
        assertFalse(serviceToken.isEmpty());

        
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(serviceToken)
                .getPayload();

        assertEquals("rental-service", claims.getSubject());
        assertEquals(-1L, claims.get("userId", Long.class));
        assertEquals("rental-service@internal", claims.get("email", String.class));
        assertEquals("OPERATOR", claims.get("role", String.class));
    }

    @Test
    void shouldGenerateServiceTokenWithCorrectExpiration() {
        
        String serviceToken = jwtService.generateServiceToken();

        
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(serviceToken)
                .getPayload();

        Date expiration = claims.getExpiration();
        Date issuedAt = claims.getIssuedAt();

        
        long difference = expiration.getTime() - issuedAt.getTime();
        assertTrue(difference >= 3500000 && difference <= 3700000); 
    }

    @Test
    void shouldHandleTokenWithMultipleRoles() {
        List<String> roles = List.of("USER", "ADMIN", "SUPPORT");
        String token = createToken("testuser", 1L, roles, 3600000L);

        
        List<?> extractedRoles = jwtService.getRolesFromToken(token);

        
        assertNotNull(extractedRoles);
        assertEquals(3, extractedRoles.size());
        assertTrue(extractedRoles.containsAll(roles));
    }

    @Test
    void shouldHandleTokenWithSpecialCharactersInUsername() {
        String username = "test.user@example.com";
        String token = createToken(username, 1L, List.of("USER"), 3600000L);

        
        String extractedUsername = jwtService.getUsernameFromToken(token);

        
        assertEquals(username, extractedUsername);
    }

    @Test
    void shouldHandleTokenWithNegativeUserId() {
        Long userId = -1L;
        String token = createToken("service", userId, List.of("OPERATOR"), 3600000L);

        
        Long extractedUserId = jwtService.getUserIdFromToken(token);

        
        assertEquals(userId, extractedUserId);
    }

    @Test
    void shouldHandleTokenWithLargeUserId() {
        Long userId = Long.MAX_VALUE;
        String token = createToken("testuser", userId, List.of("USER"), 3600000L);

        
        Long extractedUserId = jwtService.getUserIdFromToken(token);

        
        assertEquals(userId, extractedUserId);
    }

    @Test
    void shouldHandleTokenThatExpiresSoon() {
        String token = createToken("testuser", 1L, List.of("USER"), 5000L);

        
        String username = jwtService.getUsernameFromToken(token);

        
        assertEquals("testuser", username);
    }

    private String createToken(String subject, Long userId, List<String> roles, Long expirationMs) {
        Date expiration;
        if (expirationMs < 0) {
            
            expiration = new Date(System.currentTimeMillis() + expirationMs);
        } else {
            expiration = new Date(System.currentTimeMillis() + expirationMs);
        }

        return Jwts.builder()
                .subject(subject)
                .claim("username", subject)
                .claim("userId", userId)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(key)
                .compact();
    }
}

