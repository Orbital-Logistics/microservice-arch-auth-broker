package org.orbitalLogistic.maintenance.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.maintenance.application.ports.in.GetMaintenanceLogsUseCase;
import org.orbitalLogistic.maintenance.application.ports.out.MaintenanceLogRepository;
import org.orbitalLogistic.maintenance.domain.model.MaintenanceLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetMaintenanceLogsService implements GetMaintenanceLogsUseCase {

    private final MaintenanceLogRepository maintenanceLogRepository;

    @Override
    public Flux<MaintenanceLog> getAllMaintenanceLogs(int page, int size) {
        log.debug("Getting all maintenance logs, page: {}, size: {}", page, size);
        int offset = page * size;
        return maintenanceLogRepository.findAllPaginated(offset, size);
    }

    @Override
    public Mono<Long> countAll() {
        return maintenanceLogRepository.countAll();
    }

    @Override
    public Flux<MaintenanceLog> getSpacecraftMaintenanceHistory(Long spacecraftId, int page, int size) {
        log.debug("Getting maintenance history for spacecraft: {}, page: {}, size: {}", spacecraftId, page, size);
        int offset = page * size;
        return maintenanceLogRepository.findBySpacecraftIdPaginated(spacecraftId, size, offset);
    }

    @Override
    public Mono<Long> countBySpacecraftId(Long spacecraftId) {
        return maintenanceLogRepository.countBySpacecraftId(spacecraftId);
    }
}
