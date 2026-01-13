package org.orbitalLogistic.maintenance.application.ports.out;

import reactor.core.publisher.Mono;

public interface MaintenanceLogEnrichmentPort {
    Mono<String> getSpacecraftName(Long spacecraftId);
    Mono<String> getUserName(Long userId);
}
