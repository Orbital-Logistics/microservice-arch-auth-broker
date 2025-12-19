package org.orbitalLogistic.spacecraft.config;

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
    
    // @Bean
    // public CircuitBreaker userServiceCircuitBreaker(CircuitBreakerRegistry registry) {
    //     // Специфичная конфигурация для userService
    //     CircuitBreakerConfig userServiceConfig = CircuitBreakerConfig.custom()
    //         .failureRateThreshold(30)  // более чувствительный для userService
    //         .waitDurationInOpenState(Duration.ofSeconds(15))
    //         .slidingWindowSize(3)
    //         .minimumNumberOfCalls(1)
    //         .build();
        
    //     // Регистрируем конфигурацию
    //     registry.addConfiguration("userServiceConfig", userServiceConfig);
        
    //     // Создаем и возвращаем Circuit Breaker
    //     return registry.circuitBreaker("userService", "userServiceConfig");
    // }
    
    // @Bean
    // public CircuitBreaker missionServiceCircuitBreaker(CircuitBreakerRegistry registry) {
    //     CircuitBreakerConfig missionConfig = CircuitBreakerConfig.custom()
    //         .failureRateThreshold(70)  // менее чувствительный для missionService
    //         .waitDurationInOpenState(Duration.ofSeconds(120))  // 2 минуты
    //         .slidingWindowSize(50)  // большее окно для статистики
    //         .minimumNumberOfCalls(20)
    //         .slowCallDurationThreshold(Duration.ofSeconds(5))  // для долгих операций
    //         .slowCallRateThreshold(50)
    //         .build();
        
    //     registry.addConfiguration("missionConfig", missionConfig);
    //     return registry.circuitBreaker("missionService", "missionConfig");
    // }
}