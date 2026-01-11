package org.orbitalLogistic.maintenance.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FeignConfig {
    
    private static final ThreadLocal<String> authTokenHolder = new ThreadLocal<>();
    
    @Bean
    public RequestInterceptor bearerTokenRequestInterceptor() {
        return requestTemplate -> {
            try {
                String token = authTokenHolder.get();
                if (token != null && !token.isEmpty()) {
                    requestTemplate.header("Authorization", "Bearer " + token);
                    log.debug("JWT token forwarded to Feign request");
                    log.debug("Token length: {}, preview: {}...", 
                        token.length(), token.substring(0, Math.min(30, token.length())));
                } else {
                    log.warn("No Bearer token found in ThreadLocal for Feign call");
                }
            } catch (Exception e) {
                log.error("Failed to forward JWT token to Feign request", e);
            }
        };
    }
    
    public static void setAuthToken(String token) {
        authTokenHolder.set(token);
    }
    
    public static void clearAuthToken() {
        authTokenHolder.remove();
    }
}