package org.orbitalLogistic.maintenance.application.ports.in;

import org.orbitalLogistic.maintenance.domain.model.MaintenanceLog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GetMaintenanceLogsUseCase {
    Flux<MaintenanceLog> getAllMaintenanceLogs(int page, int size);
    Mono<Long> countAll();
    Flux<MaintenanceLog> getSpacecraftMaintenanceHistory(Long spacecraftId, int page, int size);
    Mono<Long> countBySpacecraftId(Long spacecraftId);
}
