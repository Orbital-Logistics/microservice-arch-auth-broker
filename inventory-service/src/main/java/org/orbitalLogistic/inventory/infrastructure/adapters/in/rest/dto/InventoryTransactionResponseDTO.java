package org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto;

import java.time.LocalDateTime;

public record InventoryTransactionResponseDTO(
        Long id,
        TransactionType transactionType,
        Long cargoId,
        String cargoName,
        Integer quantity,
        Long fromStorageUnitId,
        String fromStorageUnitCode,
        Long toStorageUnitId,
        String toStorageUnitCode,
        Long fromSpacecraftId,
        String fromSpacecraftName,
        Long toSpacecraftId,
        String toSpacecraftName,
        Long performedByUserId,
        String performedByUserName,
        LocalDateTime transactionDate,
        String reasonCode,
        String notes
) {
}
