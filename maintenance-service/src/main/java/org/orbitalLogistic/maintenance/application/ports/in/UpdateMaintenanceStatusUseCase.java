package org.orbitalLogistic.maintenance.application.ports.in;

import org.orbitalLogistic.maintenance.domain.model.MaintenanceLog;
import reactor.core.publisher.Mono;

public interface UpdateMaintenanceStatusUseCase {
    Mono<MaintenanceLog> updateMaintenanceStatus(UpdateMaintenanceStatusCommand command);
}
