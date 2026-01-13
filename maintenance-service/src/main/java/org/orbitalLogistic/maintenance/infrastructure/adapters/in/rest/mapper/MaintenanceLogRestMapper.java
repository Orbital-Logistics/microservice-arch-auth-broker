package org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.mapper;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.maintenance.application.ports.in.CreateMaintenanceLogCommand;
import org.orbitalLogistic.maintenance.application.ports.in.UpdateMaintenanceStatusCommand;
import org.orbitalLogistic.maintenance.application.ports.out.MaintenanceLogEnrichmentPort;
import org.orbitalLogistic.maintenance.domain.model.MaintenanceLog;
import org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceLogRequestDTO;
import org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceLogResponseDTO;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MaintenanceLogRestMapper {

    private final MaintenanceLogEnrichmentPort enrichmentPort;

    public CreateMaintenanceLogCommand toCreateCommand(MaintenanceLogRequestDTO dto) {
        return CreateMaintenanceLogCommand.builder()
                .spacecraftId(dto.spacecraftId())
                .maintenanceType(mapMaintenanceType(dto.maintenanceType()))
                .performedByUserId(dto.performedByUserId())
                .supervisedByUserId(dto.supervisedByUserId())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .status(mapMaintenanceStatus(dto.status()))
                .description(dto.description())
                .cost(dto.cost())
                .build();
    }

    public UpdateMaintenanceStatusCommand toUpdateCommand(Long id, MaintenanceLogRequestDTO dto) {
        return UpdateMaintenanceStatusCommand.builder()
                .id(id)
                .status(mapMaintenanceStatus(dto.status()))
                .endTime(dto.endTime())
                .cost(dto.cost())
                .description(dto.description())
                .build();
    }

    public Mono<MaintenanceLogResponseDTO> toResponseDTO(MaintenanceLog domain) {
        Mono<String> spacecraftName = enrichmentPort.getSpacecraftName(domain.getSpacecraftId());
        Mono<String> performedByName = enrichmentPort.getUserName(domain.getPerformedByUserId());
        Mono<String> supervisedByName = domain.getSupervisedByUserId() == null
                ? Mono.just("")
                : enrichmentPort.getUserName(domain.getSupervisedByUserId());

        return Mono.zip(spacecraftName, performedByName, supervisedByName)
                .map(tuple -> new MaintenanceLogResponseDTO(
                        domain.getId(),
                        domain.getSpacecraftId(),
                        tuple.getT1(),
                        mapMaintenanceTypeToDto(domain.getMaintenanceType()),
                        domain.getPerformedByUserId(),
                        tuple.getT2(),
                        domain.getSupervisedByUserId(),
                        tuple.getT3(),
                        domain.getStartTime(),
                        domain.getEndTime(),
                        mapMaintenanceStatusToDto(domain.getStatus()),
                        domain.getDescription(),
                        domain.getCost()
                ));
    }

    private org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceType mapMaintenanceType(
            org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceType dto) {
        if (dto == null) return null;
        return org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceType.valueOf(dto.name());
    }

    private org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceStatus mapMaintenanceStatus(
            org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceStatus dto) {
        if (dto == null) return null;
        return org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceStatus.valueOf(dto.name());
    }

    private org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceType mapMaintenanceTypeToDto(
            org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceType domain) {
        if (domain == null) return null;
        return org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceType.valueOf(domain.name());
    }

    private org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceStatus mapMaintenanceStatusToDto(
            org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceStatus domain) {
        if (domain == null) return null;
        return org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceStatus.valueOf(domain.name());
    }
}
