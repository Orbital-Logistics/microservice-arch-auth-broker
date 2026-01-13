package org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.mapper;

import org.orbitalLogistic.inventory.application.ports.in.CreateCargoManifestCommand;
import org.orbitalLogistic.inventory.application.ports.in.UpdateCargoManifestCommand;
import org.orbitalLogistic.inventory.domain.model.CargoManifest;
import org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.CargoManifestRequestDTO;
import org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.CargoManifestResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CargoManifestRestMapper {

    public CreateCargoManifestCommand toCreateCommand(CargoManifestRequestDTO dto) {
        return new CreateCargoManifestCommand(
                dto.spacecraftId(),
                dto.cargoId(),
                dto.storageUnitId(),
                dto.quantity(),
                dto.loadedAt(),
                dto.unloadedAt(),
                dto.loadedByUserId(),
                dto.unloadedByUserId(),
                mapManifestStatus(dto.manifestStatus()),
                mapManifestPriority(dto.priority())
        );
    }

    public UpdateCargoManifestCommand toUpdateCommand(Long id, CargoManifestRequestDTO dto) {
        return new UpdateCargoManifestCommand(
                id,
                dto.spacecraftId(),
                dto.cargoId(),
                dto.storageUnitId(),
                dto.quantity(),
                dto.loadedAt(),
                dto.unloadedAt(),
                dto.loadedByUserId(),
                dto.unloadedByUserId(),
                mapManifestStatus(dto.manifestStatus()),
                mapManifestPriority(dto.priority())
        );
    }

    public CargoManifestResponseDTO toResponseDTO(CargoManifest manifest) {
        return new CargoManifestResponseDTO(
                manifest.getId(),
                manifest.getSpacecraftId(),
                null, // enriched externally
                manifest.getCargoId(),
                null, // enriched externally
                manifest.getStorageUnitId(),
                null, // enriched externally
                manifest.getQuantity(),
                manifest.getLoadedAt(),
                manifest.getUnloadedAt(),
                manifest.getLoadedByUserId(),
                null, // enriched externally
                manifest.getUnloadedByUserId(),
                null, // enriched externally
                mapManifestStatusToRest(manifest.getManifestStatus()),
                mapManifestPriorityToRest(manifest.getPriority())
        );
    }

    private org.orbitalLogistic.inventory.domain.model.enums.ManifestStatus mapManifestStatus(
            org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.ManifestStatus dto) {
        if (dto == null) return null;
        return org.orbitalLogistic.inventory.domain.model.enums.ManifestStatus.valueOf(dto.name());
    }

    private org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.ManifestStatus mapManifestStatusToRest(
            org.orbitalLogistic.inventory.domain.model.enums.ManifestStatus domain) {
        if (domain == null) return null;
        return org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.ManifestStatus.valueOf(domain.name());
    }

    private org.orbitalLogistic.inventory.domain.model.enums.ManifestPriority mapManifestPriority(
            org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.ManifestPriority dto) {
        if (dto == null) return null;
        return org.orbitalLogistic.inventory.domain.model.enums.ManifestPriority.valueOf(dto.name());
    }

    private org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.ManifestPriority mapManifestPriorityToRest(
            org.orbitalLogistic.inventory.domain.model.enums.ManifestPriority domain) {
        if (domain == null) return null;
        return org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.ManifestPriority.valueOf(domain.name());
    }
}
