package org.orbitalLogistic.cargo.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.cargo.dto.request.StorageUnitRequestDTO;
import org.orbitalLogistic.cargo.dto.response.StorageUnitResponseDTO;
import org.orbitalLogistic.cargo.entities.StorageUnit;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StorageUnitMapper {

    @Mapping(target = "availableMassCapacity", source = "availableMassCapacity")
    @Mapping(target = "availableVolumeCapacity", source = "availableVolumeCapacity")
    @Mapping(target = "massUsagePercentage", source = "massUsagePercentage")
    @Mapping(target = "volumeUsagePercentage", source = "volumeUsagePercentage")
    StorageUnitResponseDTO toResponseDTO(
            StorageUnit storageUnit,
            BigDecimal availableMassCapacity,
            BigDecimal availableVolumeCapacity,
            Double massUsagePercentage,
            Double volumeUsagePercentage
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentMass", constant = "0")
    @Mapping(target = "currentVolume", constant = "0")
    StorageUnit toEntity(StorageUnitRequestDTO request);
}

