package org.orbitalLogistic.inventory.domain.model;

import lombok.Builder;
import lombok.Value;
import org.orbitalLogistic.inventory.domain.model.enums.TransactionType;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class InventoryTransaction {
    Long id;
    TransactionType transactionType;
    Long cargoId;
    Integer quantity;
    Long fromStorageUnitId;
    Long toStorageUnitId;
    Long fromSpacecraftId;
    Long toSpacecraftId;
    Long performedByUserId;
    LocalDateTime transactionDate;
    String reasonCode;
    String notes;

    public void validate() {
        if (transactionType == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }
        if (cargoId == null) {
            throw new IllegalArgumentException("Cargo ID is required");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (performedByUserId == null) {
            throw new IllegalArgumentException("Performed by user ID is required");
        }

        validateTransactionSpecificFields();
    }

    private void validateTransactionSpecificFields() {
        switch (transactionType) {
            case LOAD -> {
                if (toStorageUnitId == null && toSpacecraftId == null) {
                    throw new IllegalArgumentException("LOAD transaction requires a target (storage unit or spacecraft)");
                }
                if (toStorageUnitId != null && toSpacecraftId != null) {
                    throw new IllegalArgumentException("LOAD transaction cannot have both storage unit and spacecraft as target");
                }
            }
            case UNLOAD -> {
                if (fromStorageUnitId == null && fromSpacecraftId == null) {
                    throw new IllegalArgumentException("UNLOAD transaction requires a source (storage unit or spacecraft)");
                }
                if (fromStorageUnitId != null && fromSpacecraftId != null) {
                    throw new IllegalArgumentException("UNLOAD transaction cannot have both storage unit and spacecraft as source");
                }
            }
            case TRANSFER -> {
                if ((fromStorageUnitId == null && fromSpacecraftId == null) ||
                    (toStorageUnitId == null && toSpacecraftId == null)) {
                    throw new IllegalArgumentException("TRANSFER transaction requires both source and target");
                }
                if (fromStorageUnitId != null && toStorageUnitId != null &&
                    fromStorageUnitId.equals(toStorageUnitId)) {
                    throw new IllegalArgumentException("TRANSFER transaction cannot have same storage unit as source and target");
                }
                if (fromSpacecraftId != null && toSpacecraftId != null &&
                    fromSpacecraftId.equals(toSpacecraftId)) {
                    throw new IllegalArgumentException("TRANSFER transaction cannot have same spacecraft as source and target");
                }
            }
            case ADJUSTMENT, CONSUMPTION -> {
                if (fromStorageUnitId == null && fromSpacecraftId == null) {
                    throw new IllegalArgumentException(transactionType + " transaction requires a source location");
                }
            }
        }
    }

    public boolean isLoad() {
        return transactionType == TransactionType.LOAD;
    }

    public boolean isUnload() {
        return transactionType == TransactionType.UNLOAD;
    }

    public boolean isTransfer() {
        return transactionType == TransactionType.TRANSFER;
    }

    public boolean involvesSpacecraft() {
        return fromSpacecraftId != null || toSpacecraftId != null;
    }

    public boolean involvesStorage() {
        return fromStorageUnitId != null || toStorageUnitId != null;
    }
}
