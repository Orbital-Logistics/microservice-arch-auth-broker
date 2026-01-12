package org.orbitalLogistic.maintenance.application.ports.out;

import reactor.core.publisher.Mono;

public interface SpacecraftValidationPort {
    Mono<Boolean> spacecraftExists(Long spacecraftId);
}
