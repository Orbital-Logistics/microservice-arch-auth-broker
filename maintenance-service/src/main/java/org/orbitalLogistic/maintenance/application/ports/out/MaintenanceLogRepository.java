package org.orbitalLogistic.maintenance.application.ports.out;

import org.orbitalLogistic.maintenance.domain.model.MaintenanceLog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MaintenanceLogRepository {
    Mono<MaintenanceLog> save(MaintenanceLog maintenanceLog);
    Mono<MaintenanceLog> findById(Long id);
    Flux<MaintenanceLog> findAllPaginated(int offset, int size);
    Mono<Long> countAll();
    Flux<MaintenanceLog> findBySpacecraftIdPaginated(Long spacecraftId, int size, int offset);
    Mono<Long> countBySpacecraftId(Long spacecraftId);
}
