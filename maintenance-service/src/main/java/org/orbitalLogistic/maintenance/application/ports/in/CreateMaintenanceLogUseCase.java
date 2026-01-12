package org.orbitalLogistic.maintenance.application.ports.in;

import org.orbitalLogistic.maintenance.domain.model.MaintenanceLog;
import reactor.core.publisher.Mono;

public interface CreateMaintenanceLogUseCase {
    Mono<MaintenanceLog> createMaintenanceLog(CreateMaintenanceLogCommand command);
}
