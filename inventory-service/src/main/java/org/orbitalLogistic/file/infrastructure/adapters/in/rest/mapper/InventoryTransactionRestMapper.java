package org.orbitalLogistic.file.infrastructure.adapters.in.rest.mapper;

import org.orbitalLogistic.file.application.ports.in.CreateInventoryTransactionCommand;
import org.orbitalLogistic.file.domain.model.InventoryTransaction;
import org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.InventoryTransactionRequestDTO;
import org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.InventoryTransactionResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class InventoryTransactionRestMapper {

    public CreateInventoryTransactionCommand toCommand(InventoryTransactionRequestDTO dto) {
        return new CreateInventoryTransactionCommand(
                mapTransactionType(dto.transactionType()),
                dto.cargoId(),
                dto.quantity(),
                dto.fromStorageUnitId(),
                dto.toStorageUnitId(),
                dto.fromSpacecraftId(),
                dto.toSpacecraftId(),
                dto.performedByUserId(),
                dto.transactionDate(),
                dto.reasonCode(),
                dto.notes()
        );
    }

    public InventoryTransactionResponseDTO toResponseDTO(InventoryTransaction transaction) {
        return new InventoryTransactionResponseDTO(
                transaction.getId(),
                mapTransactionTypeToRest(transaction.getTransactionType()),
                transaction.getCargoId(),
                null, // enriched externally
                transaction.getQuantity(),
                transaction.getFromStorageUnitId(),
                null, // enriched externally
                transaction.getToStorageUnitId(),
                null, // enriched externally
                transaction.getFromSpacecraftId(),
                null, // enriched externally
                transaction.getToSpacecraftId(),
                null, // enriched externally
                transaction.getPerformedByUserId(),
                null, // enriched externally
                transaction.getTransactionDate(),
                transaction.getReasonCode(),
                transaction.getNotes()
        );
    }

    private org.orbitalLogistic.file.domain.model.enums.TransactionType mapTransactionType(
            org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.TransactionType dto) {
        if (dto == null) return null;
        return org.orbitalLogistic.file.domain.model.enums.TransactionType.valueOf(dto.name());
    }

    private org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.TransactionType mapTransactionTypeToRest(
            org.orbitalLogistic.file.domain.model.enums.TransactionType domain) {
        if (domain == null) return null;
        return org.orbitalLogistic.file.infrastructure.adapters.in.rest.dto.TransactionType.valueOf(domain.name());
    }
}
