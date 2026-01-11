package org.orbitalLogistic.maintenance.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.maintenance.clients.feign.SpacecraftServiceFeignClient;
import org.orbitalLogistic.maintenance.config.FeignConfig;
import org.orbitalLogistic.maintenance.dto.common.SpacecraftDTO;
import org.orbitalLogistic.maintenance.filter.JwtAuthFilter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpacecraftServiceClient {
    private final SpacecraftServiceFeignClient spacecraftServiceFeignClient;

    public Mono<SpacecraftDTO> getSpacecraftById(Long id) {
        return Mono.deferContextual(contextView -> {
            String token = contextView.getOrDefault(JwtAuthFilter.JWT_TOKEN_KEY, "");
            return Mono.fromCallable(() -> {
                try {
                    FeignConfig.setAuthToken(token);
                    return spacecraftServiceFeignClient.getSpacecraftById(id);
                } finally {
                    FeignConfig.clearAuthToken();
                }
            }).subscribeOn(Schedulers.boundedElastic());
        })
        .onErrorResume(e -> {
            log.error("Error calling spacecraft-service for id {}: {}", id, e.getMessage());
            return Mono.empty();
        });
    }

    public Mono<Boolean> spacecraftExists(Long id) {
        return Mono.deferContextual(contextView -> {
            String token = contextView.getOrDefault(JwtAuthFilter.JWT_TOKEN_KEY, "");
            return Mono.fromCallable(() -> {
                try {
                    FeignConfig.setAuthToken(token);
                    return spacecraftServiceFeignClient.spacecraftExists(id);
                } finally {
                    FeignConfig.clearAuthToken();
                }
            }).subscribeOn(Schedulers.boundedElastic());
        })
        .onErrorResume(e -> {
            log.error("Error checking spacecraft exists for id {}: {}", id, e.getMessage());
            return Mono.just(false);
        });
    }
}
