package org.orbitalLogistic.maintenance.clients.spacecraft;

import org.orbitalLogistic.maintenance.dto.common.SpacecraftDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class SpacecraftServiceClient {

    private final WebClient webClient;
    private final SpacecraftServiceClientFallback fallback;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public SpacecraftServiceClient(
        SpacecraftServiceClientFallback fallback,
        WebClient.Builder webClientBuilder, 
        CircuitBreakerRegistry circuitBreakerRegistry) {
        this.webClient = webClientBuilder
                .baseUrl("http://spacecraft-service/api/spacecrafts")
                .build();
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.fallback = fallback;
    }

    public Mono<SpacecraftDTO> getSpacecraftById(Long id) {
        return webClient.get()
            .uri("/{id}", id)
            .retrieve()
            .bodyToMono(SpacecraftDTO.class)
            .transformDeferred(CircuitBreakerOperator.of(
                circuitBreakerRegistry.circuitBreaker("spacecraftService")
            ))
            .doOnError(e -> log.error("Error calling spacecraft-service: {}", e.getMessage()))
            // Обработка 404 - Spacecraft не найден
            .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                log.warn("Spacecraft id {} not found", id);
                // Возвращаем fallback SpacecraftDTO
                return Mono.empty();
            })
            // Обработка других ошибок 4xx/5xx
            .onErrorResume(WebClientResponseException.class, e -> {
                log.error("HTTP error {} when calling spacecraft-service: {}",
                        e.getStatusCode(), e.getMessage());
                return fallback.getSpacecraftById(id, e);
            })
            // Общий fallback для всех остальных ошибок
            .onErrorResume(e -> {
                log.error("Unexpected error calling spacecraft-service: {}", e.getMessage());
                return fallback.getSpacecraftById(id, e);
            });
        }

    public Mono<Boolean> spacecraftExists(Long id) {
        return webClient.get()
                .uri("/{id}/exists", id)
                .retrieve()
                .bodyToMono(Boolean.class)
                .transformDeferred(CircuitBreakerOperator.of(
                    circuitBreakerRegistry.circuitBreaker("spacecraftService")
                ))
                .doOnError(e -> log.error("Error calling spacecraft-service: {}", e.getMessage()))
                .onErrorResume(ex -> {
                    log.error("FALLBACK spacecraftExists! id: {}, error: {}", id, ex.getClass().getSimpleName());
                    return Mono.empty();
                });
    }
}

