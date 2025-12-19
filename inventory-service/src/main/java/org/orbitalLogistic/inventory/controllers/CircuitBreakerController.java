package org.orbitalLogistic.inventory.controllers;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/circuit-breaker")
@RequiredArgsConstructor
public class CircuitBreakerController {
    private final CircuitBreakerRegistry registry;

    @GetMapping("/config")
    public Map<String, Object> getConfigCB(@RequestParam String name){
        CircuitBreaker cb = registry.circuitBreaker(name);
        if (cb == null) {
            return Map.of("error", "CircuitBreaker not found: " + name);
        }   
        
        return Map.of(
            "name", name,
            "state", cb.getState(),
            "config", Map.of(
                "failureRateThreshold", cb.getCircuitBreakerConfig().getFailureRateThreshold(),
                "slidingWindowSize", cb.getCircuitBreakerConfig().getSlidingWindowSize(),
                "minimumNumberOfCalls", cb.getCircuitBreakerConfig().getMinimumNumberOfCalls(),
                "waitDurationInOpenState", cb.getCircuitBreakerConfig().getWaitIntervalFunctionInOpenState(),
                "permittedNumberOfCallsInHalfOpenState", cb.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState(),
                "slidingWindowType", cb.getCircuitBreakerConfig().getSlidingWindowType()
            )
        );
    }
}
