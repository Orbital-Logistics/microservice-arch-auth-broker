package org.orbitalLogistic.maintenance.jwt;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {

    private final JwtService jwtService;
    public static final String JWT_TOKEN_KEY = "JWT_TOKEN";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        try {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return chain.filter(exchange);
            }

            String jwt = authHeader.substring(7);

            if (!jwtService.validateToken(jwt)) {
                return chain.filter(exchange);
            }

            Claims claims = jwtService.extractAllClaims(jwt);
            List<String> roles = claims.get("roles", List.class);

            List<GrantedAuthority> authorities = (roles == null ? List.<String>of() : roles).stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            String username = claims.getSubject();

            Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContext securityContext = new SecurityContextImpl(auth);

            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                    .contextWrite(Context.of(JWT_TOKEN_KEY, jwt));
        } catch (Exception e) {
            return chain.filter(exchange);
        }
    }
}
