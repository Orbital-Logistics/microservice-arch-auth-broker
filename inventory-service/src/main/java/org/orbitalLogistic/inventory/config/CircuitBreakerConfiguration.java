package org.orbitalLogistic.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;

@Configuration
public class CircuitBreakerConfiguration {
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(1)
            .waitDurationInOpenState(Duration.ofSeconds(5))
            .slidingWindowSize(1)
            .minimumNumberOfCalls(1)
            .enableAutomaticTransitionFromOpenToHalfOpen()
            .build();
        
        return CircuitBreakerRegistry.of(defaultConfig);
    }
}