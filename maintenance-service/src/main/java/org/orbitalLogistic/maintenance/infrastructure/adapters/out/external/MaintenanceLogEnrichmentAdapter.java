package org.orbitalLogistic.maintenance.infrastructure.adapters.out.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.maintenance.application.ports.out.MaintenanceLogEnrichmentPort;
import org.orbitalLogistic.maintenance.clients.SpacecraftServiceClient;
import org.orbitalLogistic.maintenance.clients.UserServiceClient;
import org.orbitalLogistic.maintenance.infrastructure.adapters.out.external.dto.SpacecraftDTO;
import org.orbitalLogistic.maintenance.infrastructure.adapters.out.external.dto.UserDTO;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceLogEnrichmentAdapter implements MaintenanceLogEnrichmentPort {

    private final SpacecraftServiceClient spacecraftServiceClient;
    private final UserServiceClient userServiceClient;

    @Override
    public Mono<String> getSpacecraftName(Long spacecraftId) {
        return spacecraftServiceClient.getSpacecraftById(spacecraftId)
                .map(SpacecraftDTO::name)
                .onErrorResume(ex -> {
                    log.warn("Fallback: Unable to fetch spacecraft with id: {}, error: {}", spacecraftId, ex.getMessage());
                    return Mono.just("Unknown");
                })
                .defaultIfEmpty("Unknown");
    }

    @Override
    public Mono<String> getUserName(Long userId) {
        return userServiceClient.getUserById(userId)
                .map(UserDTO::username)
                .onErrorResume(ex -> {
                    log.warn("Fallback: Unable to fetch user with id: {}, error: {}", userId, ex.getMessage());
                    return Mono.just("Unknown");
                })
                .defaultIfEmpty("Unknown");
    }
}
