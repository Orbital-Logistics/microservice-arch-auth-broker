package org.orbitalLogistic.maintenance.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.maintenance.clients.feign.UserServiceFeignClient;
import org.orbitalLogistic.maintenance.config.FeignConfig;
import org.orbitalLogistic.maintenance.dto.common.UserDTO;
import org.orbitalLogistic.maintenance.filter.JwtAuthFilter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    private final UserServiceFeignClient userServiceFeignClient;

    public Mono<UserDTO> getUserById(Long id) {
        return Mono.deferContextual(contextView -> {
            String token = contextView.getOrDefault(JwtAuthFilter.JWT_TOKEN_KEY, "");
            return Mono.fromCallable(() -> {
                try {
                    FeignConfig.setAuthToken(token);
                    return userServiceFeignClient.getUserById(id);
                } finally {
                    FeignConfig.clearAuthToken();
                }
            }).subscribeOn(Schedulers.boundedElastic());
        })
        .onErrorResume(e -> {
            log.error("Error calling user-service for id {}: {}", id, e.getMessage());
            return Mono.empty();
        });
    }

    public Mono<Boolean> userExists(Long id) {
        return Mono.deferContextual(contextView -> {
            String token = contextView.getOrDefault(JwtAuthFilter.JWT_TOKEN_KEY, "");
            return Mono.fromCallable(() -> {
                try {
                    FeignConfig.setAuthToken(token);
                    return userServiceFeignClient.userExists(id);
                } finally {
                    FeignConfig.clearAuthToken();
                }
            }).subscribeOn(Schedulers.boundedElastic());
        })
        .onErrorResume(e -> {
            log.error("Error checking user exists for id {}: {}", id, e.getMessage());
            return Mono.just(false);
        });
    }
}
