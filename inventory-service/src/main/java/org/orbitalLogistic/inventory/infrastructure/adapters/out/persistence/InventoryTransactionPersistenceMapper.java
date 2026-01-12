package org.orbitalLogistic.inventory.infrastructure.adapters.out.persistence;

import org.orbitalLogistic.inventory.domain.model.InventoryTransaction;
import org.orbitalLogistic.inventory.domain.model.enums.TransactionType;
import org.springframework.stereotype.Component;

@Component
public class InventoryTransactionPersistenceMapper {

    public InventoryTransactionEntity toEntity(InventoryTransaction transaction) {
        return InventoryTransactionEntity.builder()
                .id(transaction.getId())
                .transactionType(transaction.getTransactionType() != null ? transaction.getTransactionType().name() : null)
                .cargoId(transaction.getCargoId())
                .quantity(transaction.getQuantity())
                .fromStorageUnitId(transaction.getFromStorageUnitId())
                .toStorageUnitId(transaction.getToStorageUnitId())
                .fromSpacecraftId(transaction.getFromSpacecraftId())
                .toSpacecraftId(transaction.getToSpacecraftId())
                .performedByUserId(transaction.getPerformedByUserId())
                .transactionDate(transaction.getTransactionDate())
                .reasonCode(transaction.getReasonCode())
                .notes(transaction.getNotes())
                .build();
    }

    public InventoryTransaction toDomain(InventoryTransactionEntity entity) {
        return InventoryTransaction.builder()
                .id(entity.getId())
                .transactionType(entity.getTransactionType() != null ? TransactionType.valueOf(entity.getTransactionType()) : null)
                .cargoId(entity.getCargoId())
                .quantity(entity.getQuantity())
                .fromStorageUnitId(entity.getFromStorageUnitId())
                .toStorageUnitId(entity.getToStorageUnitId())
                .fromSpacecraftId(entity.getFromSpacecraftId())
                .toSpacecraftId(entity.getToSpacecraftId())
                .performedByUserId(entity.getPerformedByUserId())
                .transactionDate(entity.getTransactionDate())
                .reasonCode(entity.getReasonCode())
                .notes(entity.getNotes())
                .build();
    }
}
