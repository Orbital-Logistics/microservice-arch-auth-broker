package org.orbitalLogistic.file.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
public class FeignConfig {
    
    @Bean
    public RequestInterceptor bearerTokenRequestInterceptor() {
        return requestTemplate -> {
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) 
                    RequestContextHolder.getRequestAttributes();
                
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String authHeader = request.getHeader("Authorization");
                    
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        requestTemplate.header("Authorization", authHeader);
                        log.debug("JWT token forwarded to Feign request");
                        
                        String token = authHeader.substring(7);
                        log.debug("Token length: {}, preview: {}...", 
                            token.length(), token.substring(0, Math.min(30, token.length())));
                    } else {
                        log.warn("No Bearer token found in current request for Feign call");
                    }
                } else {
                    log.warn("No HTTP request context found for Feign call");
                }
            } catch (Exception e) {
                log.error("Failed to forward JWT token to Feign request", e);
            }
        };
    }
}