package org.orbitalLogistic.user.infrastructure.adapters.in.rest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.user.infrastructure.adapters.out.security.JwtTokenAdapter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenAdapter jwtTokenAdapter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String jwt = authHeader.substring(7);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    String username = jwtTokenAdapter.extractUsername(jwt);
                    java.util.List<String> roles = jwtTokenAdapter.extractRoles(jwt);
                    
                    SecurityContext context = SecurityContextHolder.createEmptyContext();

                    java.util.List<GrantedAuthority> authorities = roles.stream()
                            .map(role -> role.startsWith("ROLE_") ? 
                                new SimpleGrantedAuthority(role) : 
                                new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(java.util.stream.Collectors.toList());

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    context.setAuthentication(authToken);
                    SecurityContextHolder.setContext(context);
                } catch (Exception e) {
                    log.error("JWT token validation failed: {}", e.getMessage());
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage());
            filterChain.doFilter(request, response);
        }
    }
}
