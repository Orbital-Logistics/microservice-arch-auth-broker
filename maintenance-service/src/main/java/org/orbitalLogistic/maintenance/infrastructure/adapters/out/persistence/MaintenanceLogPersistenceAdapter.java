package org.orbitalLogistic.maintenance.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.maintenance.application.ports.out.MaintenanceLogRepository;
import org.orbitalLogistic.maintenance.domain.model.MaintenanceLog;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MaintenanceLogPersistenceAdapter implements MaintenanceLogRepository {

    private final MaintenanceLogR2dbcRepository r2dbcRepository;
    private final MaintenanceLogPersistenceMapper mapper;

    @Override
    public Mono<MaintenanceLog> save(MaintenanceLog maintenanceLog) {
        MaintenanceLogEntity entity = mapper.toEntity(maintenanceLog);
        return r2dbcRepository.save(entity)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<MaintenanceLog> findById(Long id) {
        return r2dbcRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<MaintenanceLog> findAllPaginated(int offset, int size) {
        return r2dbcRepository.findAllPaginated(offset, size)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countAll() {
        return r2dbcRepository.countAll();
    }

    @Override
    public Flux<MaintenanceLog> findBySpacecraftIdPaginated(Long spacecraftId, int size, int offset) {
        return r2dbcRepository.findBySpacecraftIdPaginated(spacecraftId, size, offset)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countBySpacecraftId(Long spacecraftId) {
        return r2dbcRepository.countBySpacecraftId(spacecraftId);
    }
}
