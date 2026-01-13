package org.orbitalLogistic.inventory.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should pass through when no Authorization header")
    void doFilterInternal_NoAuthHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).validateToken(any());
    }

    @Test
    @DisplayName("Should pass through when Authorization header does not start with Bearer")
    void doFilterInternal_NotBearerAuth() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).validateToken(any());
    }

    @Test
    @DisplayName("Should authenticate with valid token")
    void doFilterInternal_ValidToken() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.validateToken(token)).thenReturn(true);

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(username);
        when(claims.get("roles", List.class)).thenReturn(List.of("USER", "ADMIN"));
        when(jwtService.extractAllClaims(token)).thenReturn(claims);
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(username, authentication.getPrincipal());
        assertEquals(2, authentication.getAuthorities().size());
        assertTrue(authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_USER")));
        assertTrue(authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Should handle token with roles already prefixed with ROLE_")
    void doFilterInternal_RolesWithPrefix() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.validateToken(token)).thenReturn(true);

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("testuser");
        when(claims.get("roles", List.class)).thenReturn(List.of("ROLE_USER", "ADMIN"));
        when(jwtService.extractAllClaims(token)).thenReturn(claims);
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(2, authentication.getAuthorities().size());
        assertTrue(authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .allMatch(auth -> auth.startsWith("ROLE_")));
    }

    @Test
    @DisplayName("Should handle token without roles claim")
    void doFilterInternal_NoRoles() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.validateToken(token)).thenReturn(true);

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("testuser");
        when(claims.get("roles", List.class)).thenReturn(null);
        when(jwtService.extractAllClaims(token)).thenReturn(claims);
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("Should not authenticate with invalid token")
    void doFilterInternal_InvalidToken() throws ServletException, IOException {
        String token = "invalid.jwt.token";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.validateToken(token)).thenReturn(false);
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extractAllClaims(any());
    }

    @Test
    @DisplayName("Should not override existing authentication")
    void doFilterInternal_ExistingAuthentication() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;

        Authentication existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).validateToken(any());
    }

    @Test
    @DisplayName("Should handle exception gracefully")
    void doFilterInternal_ExceptionHandling() throws ServletException, IOException {
        String authHeader = "Bearer token";
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.validateToken(any())).thenThrow(new RuntimeException("JWT error"));
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should handle empty roles list")
    void doFilterInternal_EmptyRolesList() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.validateToken(token)).thenReturn(true);

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("testuser");
        when(claims.get("roles", List.class)).thenReturn(List.of());
        when(jwtService.extractAllClaims(token)).thenReturn(claims);
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().isEmpty());
    }
}
