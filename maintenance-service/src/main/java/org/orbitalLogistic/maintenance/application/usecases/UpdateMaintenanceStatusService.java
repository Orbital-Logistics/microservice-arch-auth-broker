package org.orbitalLogistic.maintenance.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.maintenance.application.ports.in.UpdateMaintenanceStatusCommand;
import org.orbitalLogistic.maintenance.application.ports.in.UpdateMaintenanceStatusUseCase;
import org.orbitalLogistic.maintenance.application.ports.out.MaintenanceLogRepository;
import org.orbitalLogistic.maintenance.domain.model.MaintenanceLog;
import org.orbitalLogistic.maintenance.exceptions.MaintenanceLogNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateMaintenanceStatusService implements UpdateMaintenanceStatusUseCase {

    private final MaintenanceLogRepository maintenanceLogRepository;

    @Override
    public Mono<MaintenanceLog> updateMaintenanceStatus(UpdateMaintenanceStatusCommand command) {
        log.debug("Updating maintenance log status, id: {}", command.id());

        return maintenanceLogRepository.findById(command.id())
                .switchIfEmpty(Mono.error(new MaintenanceLogNotFoundException("Maintenance log not found with id: " + command.id())))
                .map(existing -> updateFields(existing, command))
                .doOnNext(MaintenanceLog::validate)
                .flatMap(maintenanceLogRepository::save)
                .doOnSuccess(updatedLog -> log.info("Maintenance log updated with id: {}", updatedLog.getId()));
    }

    private MaintenanceLog updateFields(MaintenanceLog existing, UpdateMaintenanceStatusCommand command) {
        return existing.toBuilder()
                .status(command.status() != null ? command.status() : existing.getStatus())
                .endTime(command.endTime() != null ? command.endTime() : existing.getEndTime())
                .cost(command.cost() != null ? command.cost() : existing.getCost())
                .description(command.description() != null ? command.description() : existing.getDescription())
                .build();
    }
}
