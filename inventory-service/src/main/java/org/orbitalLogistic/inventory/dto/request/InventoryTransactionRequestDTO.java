package org.orbitalLogistic.inventory.dto.request;

import org.orbitalLogistic.inventory.entities.enums.TransactionType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record InventoryTransactionRequestDTO(

        @NotNull(message = "Transaction type is required")
        TransactionType transactionType,

        @NotNull(message = "Cargo ID is required")
        Long cargoId,

        @NotNull
        @Min(value = 1, message = "Quantity must be positive")
        Integer quantity,

        Long fromStorageUnitId,
        Long toStorageUnitId,
        Long fromSpacecraftId,
        Long toSpacecraftId,

        @NotNull(message = "Performed by user ID is required")
        Long performedByUserId,

        LocalDateTime transactionDate,

        @Size(max = 50, message = "Reason code must not exceed 50 characters")
        String reasonCode,

        @Size(max = 100, message = "Reference number must not exceed 100 characters")
        String referenceNumber,

        String notes
) {}










