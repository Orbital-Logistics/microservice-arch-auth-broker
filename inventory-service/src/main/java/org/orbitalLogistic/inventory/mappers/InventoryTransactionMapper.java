package org.orbitalLogistic.inventory.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.inventory.dto.request.InventoryTransactionRequestDTO;
import org.orbitalLogistic.inventory.dto.response.InventoryTransactionResponseDTO;
import org.orbitalLogistic.inventory.entities.InventoryTransaction;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventoryTransactionMapper {

    @Mapping(target = "cargoName", source = "cargoName")
    @Mapping(target = "fromStorageUnitCode", source = "fromStorageUnitCode")
    @Mapping(target = "toStorageUnitCode", source = "toStorageUnitCode")
    @Mapping(target = "fromSpacecraftName", source = "fromSpacecraftName")
    @Mapping(target = "toSpacecraftName", source = "toSpacecraftName")
    @Mapping(target = "performedByUserName", source = "performedByUserName")
    InventoryTransactionResponseDTO toResponseDTO(
        InventoryTransaction transaction,
        String cargoName,
        String fromStorageUnitCode,
        String toStorageUnitCode,
        String fromSpacecraftName,
        String toSpacecraftName,
        String performedByUserName
    );

    @Mapping(target = "id", ignore = true)
    InventoryTransaction toEntity(InventoryTransactionRequestDTO request);
}

