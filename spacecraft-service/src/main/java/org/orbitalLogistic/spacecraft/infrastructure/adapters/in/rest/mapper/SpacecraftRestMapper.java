package org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.mapper;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.spacecraft.application.ports.in.CreateSpacecraftCommand;
import org.orbitalLogistic.spacecraft.application.ports.in.GetSpacecraftTypesUseCase;
import org.orbitalLogistic.spacecraft.application.ports.in.UpdateSpacecraftCommand;
import org.orbitalLogistic.spacecraft.clients.ResilientCargoServiceClient;
import org.orbitalLogistic.spacecraft.domain.model.Spacecraft;
import org.orbitalLogistic.spacecraft.domain.model.SpacecraftType;
import org.orbitalLogistic.spacecraft.dto.common.SpacecraftCargoUsageDTO;
import org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.dto.SpacecraftRequestDTO;
import org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.dto.SpacecraftResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class SpacecraftRestMapper {

    private final GetSpacecraftTypesUseCase getSpacecraftTypesUseCase;
    private final ResilientCargoServiceClient cargoServiceClient;

    public CreateSpacecraftCommand toCreateCommand(SpacecraftRequestDTO dto) {
        return new CreateSpacecraftCommand(
                dto.registryCode(),
                dto.name(),
                dto.spacecraftTypeId(),
                dto.massCapacity(),
                dto.volumeCapacity(),
                dto.status(),
                dto.currentLocation()
        );
    }

    public UpdateSpacecraftCommand toUpdateCommand(Long id, SpacecraftRequestDTO dto) {
        return new UpdateSpacecraftCommand(
                id,
                dto.registryCode(),
                dto.name(),
                dto.spacecraftTypeId(),
                dto.massCapacity(),
                dto.volumeCapacity(),
                dto.status(),
                dto.currentLocation()
        );
    }

    public SpacecraftResponseDTO toResponseDTO(Spacecraft spacecraft) {
        SpacecraftType spacecraftType = getSpacecraftTypesUseCase.getSpacecraftTypeById(spacecraft.getSpacecraftTypeId());

        BigDecimal currentMassUsage = BigDecimal.ZERO;
        BigDecimal currentVolumeUsage = BigDecimal.ZERO;

        try {
            SpacecraftCargoUsageDTO cargoUsage = cargoServiceClient.getSpacecraftCargoUsage(spacecraft.getId());
            if (cargoUsage != null) {
                currentMassUsage = cargoUsage.currentMassUsage();
                currentVolumeUsage = cargoUsage.currentVolumeUsage();
            }
        } catch (Exception e) {
            // Log but don't fail - cargo usage is optional
        }

        return new SpacecraftResponseDTO(
                spacecraft.getId(),
                spacecraft.getRegistryCode(),
                spacecraft.getName(),
                spacecraft.getSpacecraftTypeId(),
                spacecraftType.getTypeName(),
                spacecraft.getMassCapacity(),
                spacecraft.getVolumeCapacity(),
                spacecraft.getStatus(),
                spacecraft.getCurrentLocation(),
                currentMassUsage,
                currentVolumeUsage
        );
    }
}

