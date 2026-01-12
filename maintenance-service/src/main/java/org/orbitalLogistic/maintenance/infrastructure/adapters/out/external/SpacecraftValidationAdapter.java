package org.orbitalLogistic.maintenance.infrastructure.adapters.out.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.maintenance.application.ports.out.SpacecraftValidationPort;
import org.orbitalLogistic.maintenance.clients.SpacecraftServiceClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpacecraftValidationAdapter implements SpacecraftValidationPort {

    private final SpacecraftServiceClient spacecraftServiceClient;

    @Override
    public Mono<Boolean> spacecraftExists(Long spacecraftId) {
        return spacecraftServiceClient.spacecraftExists(spacecraftId);
    }
}
