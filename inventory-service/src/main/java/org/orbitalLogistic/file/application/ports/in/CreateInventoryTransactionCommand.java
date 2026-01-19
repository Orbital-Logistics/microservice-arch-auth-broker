package org.orbitalLogistic.file.application.ports.in;

import org.orbitalLogistic.file.domain.model.enums.TransactionType;

import java.time.LocalDateTime;

public record CreateInventoryTransactionCommand(
        TransactionType transactionType,
        Long cargoId,
        Integer quantity,
        Long fromStorageUnitId,
        Long toStorageUnitId,
        Long fromSpacecraftId,
        Long toSpacecraftId,
        Long performedByUserId,
        LocalDateTime transactionDate,
        String reasonCode,
        String notes
) {
}
