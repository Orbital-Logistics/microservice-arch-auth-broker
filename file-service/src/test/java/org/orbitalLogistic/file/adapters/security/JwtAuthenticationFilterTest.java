package org.orbitalLogistic.file.adapters.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateUserWithValidToken() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;
        
        when(request.getRequestURI()).thenReturn("/api/files/upload");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
        when(jwtService.getSubjectFromToken(token)).thenReturn("testuser");
        doReturn(Arrays.asList("USER", "ADMIN")).when(jwtService).getRolesFromToken(token);
        when(jwtService.getUserIdFromToken(token)).thenReturn(123L);

        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        assertEquals("testuser", principal.getUsername());
        assertEquals(123L, principal.getUserId());
        
        assertEquals(2, authentication.getAuthorities().size());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldContinueFilterChainWhenNoAuthHeader() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/files/upload");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).getSubjectFromToken(anyString());
    }

    @Test
    void shouldContinueFilterChainWhenNotBearerToken() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/files/upload");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic sometoken");

        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).getSubjectFromToken(anyString());
    }

    @Test
    void shouldHandleExpiredTokenGracefully() throws ServletException, IOException {
        String token = "expired.jwt.token";
        String authHeader = "Bearer " + token;
        
        when(request.getRequestURI()).thenReturn("/api/files/upload");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
        when(jwtService.getSubjectFromToken(token)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldHandleInvalidTokenGracefully() throws ServletException, IOException {
        String token = "invalid.jwt.token";
        String authHeader = "Bearer " + token;
        
        when(request.getRequestURI()).thenReturn("/api/files/upload");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
        when(jwtService.getSubjectFromToken(token)).thenThrow(new RuntimeException("Invalid token"));

        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSetUserPrincipalWithCorrectDetails() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;
        Long userId = 999L;
        String username = "john.doe";
        List<?> roles = Arrays.asList("SUPPORT");
        
        when(request.getRequestURI()).thenReturn("/api/files/get-files-list");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
        when(jwtService.getSubjectFromToken(token)).thenReturn(username);
        doReturn(roles).when(jwtService).getRolesFromToken(token);
        when(jwtService.getUserIdFromToken(token)).thenReturn(userId);

        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        assertEquals(username, principal.getUsername());
        assertEquals(userId, principal.getUserId());
        assertEquals(roles, principal.getRoles());
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldHandleServiceAccountToken() throws ServletException, IOException {
        String token = "service.account.token";
        String authHeader = "Bearer " + token;
        
        when(request.getRequestURI()).thenReturn("/api/files/upload");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
        when(jwtService.getSubjectFromToken(token)).thenReturn("rental-service");
        doReturn(Arrays.asList("OPERATOR")).when(jwtService).getRolesFromToken(token);
        when(jwtService.getUserIdFromToken(token)).thenReturn(-1L);

        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        assertEquals("rental-service", principal.getUsername());
        assertEquals(-1L, principal.getUserId());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_OPERATOR")));
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldHandleTokenWithMultipleRoles() throws ServletException, IOException {
        String token = "multi.role.token";
        String authHeader = "Bearer " + token;
        List<?> roles = Arrays.asList("USER", "ADMIN", "SUPPORT");
        
        when(request.getRequestURI()).thenReturn("/api/files/remove-file");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
        when(jwtService.getSubjectFromToken(token)).thenReturn("superuser");
        doReturn(roles).when(jwtService).getRolesFromToken(token);
        when(jwtService.getUserIdFromToken(token)).thenReturn(1L);

        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(3, authentication.getAuthorities().size());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPPORT")));
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldExtractTokenCorrectlyFromBearerHeader() throws ServletException, IOException {
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        String authHeader = "Bearer " + expectedToken;
        
        when(request.getRequestURI()).thenReturn("/api/files/upload");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
        when(jwtService.getSubjectFromToken(expectedToken)).thenReturn("testuser");
        doReturn(Arrays.asList("USER")).when(jwtService).getRolesFromToken(expectedToken);
        when(jwtService.getUserIdFromToken(expectedToken)).thenReturn(1L);

        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        
        verify(jwtService).getSubjectFromToken(expectedToken);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTokenExtractionFails() throws ServletException, IOException {
        String token = "malformed.token";
        String authHeader = "Bearer " + token;
        
        when(request.getRequestURI()).thenReturn("/api/files/upload");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
        when(jwtService.getSubjectFromToken(token))
                .thenThrow(new IllegalArgumentException("Malformed token"));

        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldHandleEmptyBearerToken() throws ServletException, IOException {
        String authHeader = "Bearer ";
        
        when(request.getRequestURI()).thenReturn("/api/files/upload");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);

        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        
        verify(filterChain).doFilter(request, response);
    }
}


