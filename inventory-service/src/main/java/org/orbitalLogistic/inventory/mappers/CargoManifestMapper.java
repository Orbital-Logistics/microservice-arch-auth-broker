package org.orbitalLogistic.inventory.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.inventory.dto.request.CargoManifestRequestDTO;
import org.orbitalLogistic.inventory.dto.response.CargoManifestResponseDTO;
import org.orbitalLogistic.inventory.entities.CargoManifest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CargoManifestMapper {

    @Mapping(target = "spacecraftName", source = "spacecraftName")
    @Mapping(target = "cargoName", source = "cargoName")
    @Mapping(target = "storageUnitCode", source = "storageUnitCode")
    @Mapping(target = "loadedByUserName", source = "loadedByUserName")
    @Mapping(target = "unloadedByUserName", source = "unloadedByUserName")
    CargoManifestResponseDTO toResponseDTO(
        CargoManifest manifest,
        String spacecraftName,
        String cargoName,
        String storageUnitCode,
        String loadedByUserName,
        String unloadedByUserName
    );

    @Mapping(target = "id", ignore = true)
    CargoManifest toEntity(CargoManifestRequestDTO request);
}

