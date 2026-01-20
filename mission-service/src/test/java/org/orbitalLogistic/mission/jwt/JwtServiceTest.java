package org.orbitalLogistic.mission.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private String secretKey;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        secretKey = "yourSuperSecretKeyAtLeast256BitsLongForProductionChangeThis";
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    @DisplayName("validateToken должен вернуть true для валидного токена")
    void validateToken_ValidToken_ReturnsTrue() {
        String token = generateToken("testUser", 1000000);

        boolean result = jwtService.validateToken(token);

        assertTrue(result);
    }

    @Test
    @DisplayName("validateToken должен вернуть false для истекшего токена")
    void validateToken_ExpiredToken_ReturnsFalse() {
        String token = generateToken("testUser", -1000);

        boolean result = jwtService.validateToken(token);

        assertFalse(result);
    }

    @Test
    @DisplayName("validateToken должен вернуть false для невалидного токена")
    void validateToken_InvalidToken_ReturnsFalse() {
        String invalidToken = "invalid.token.here";

        boolean result = jwtService.validateToken(invalidToken);

        assertFalse(result);
    }

    @Test
    @DisplayName("validateToken должен вернуть false для null токена")
    void validateToken_NullToken_ReturnsFalse() {
        boolean result = jwtService.validateToken(null);

        assertFalse(result);
    }

    @Test
    @DisplayName("validateToken должен вернуть false для пустого токена")
    void validateToken_EmptyToken_ReturnsFalse() {
        boolean result = jwtService.validateToken("");

        assertFalse(result);
    }

    @Test
    @DisplayName("extractAllClaims должен извлечь claims из валидного токена")
    void extractAllClaims_ValidToken_ExtractsClaims() {
        String username = "testUser";
        String token = generateToken(username, 1000000);

        Claims claims = jwtService.extractAllClaims(token);

        assertNotNull(claims);
        assertThat(claims.getSubject()).isEqualTo(username);
        assertNotNull(claims.getExpiration());
        assertNotNull(claims.getIssuedAt());
    }

    @Test
    @DisplayName("extractAllClaims должен выбросить исключение для невалидного токена")
    void extractAllClaims_InvalidToken_ThrowsException() {
        String invalidToken = "invalid.token.here";

        assertThrows(Exception.class, () -> jwtService.extractAllClaims(invalidToken));
    }

    @Test
    @DisplayName("validateToken должен корректно обработать токен с дополнительными claims")
    void validateToken_TokenWithCustomClaims_ReturnsTrue() {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 1000000);

        String token = Jwts.builder()
                .setSubject("testUser")
                .claim("role", "ADMIN")
                .claim("userId", 123L)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(signingKey)
                .compact();

        boolean result = jwtService.validateToken(token);

        assertTrue(result);
    }

    @Test
    @DisplayName("extractAllClaims должен извлечь кастомные claims")
    void extractAllClaims_TokenWithCustomClaims_ExtractsAllClaims() {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 1000000);

        String token = Jwts.builder()
                .setSubject("testUser")
                .claim("role", "ADMIN")
                .claim("userId", 123)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(signingKey)
                .compact();

        Claims claims = jwtService.extractAllClaims(token);

        assertThat(claims.getSubject()).isEqualTo("testUser");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("userId", Integer.class)).isEqualTo(123);
    }

    @Test
    @DisplayName("validateToken должен обработать токен на грани истечения")
    void validateToken_TokenAlmostExpired_ReturnsTrue() {
        String token = generateToken("testUser", 5000);

        boolean result = jwtService.validateToken(token);

        assertTrue(result);
    }

    @Test
    @DisplayName("validateToken должен вернуть false для токена с неправильной подписью")
    void validateToken_TokenWithWrongSignature_ReturnsFalse() {
        SecretKey wrongKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                "differentSecretKeyForTestingPurposesAtLeast256BitsLong"
        ));

        Date now = new Date();
        Date expiration = new Date(now.getTime() + 1000000);

        String tokenWithWrongSignature = Jwts.builder()
                .setSubject("testUser")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(wrongKey)
                .compact();

        boolean result = jwtService.validateToken(tokenWithWrongSignature);

        assertFalse(result);
    }

    private String generateToken(String username, long expirationOffset) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationOffset);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(signingKey)
                .compact();
    }
}
