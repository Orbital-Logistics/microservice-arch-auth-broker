package org.orbitalLogistic.file.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.file.application.ports.in.CreateInventoryTransactionCommand;
import org.orbitalLogistic.file.application.ports.in.CreateInventoryTransactionUseCase;
import org.orbitalLogistic.file.application.ports.out.CargoValidationPort;
import org.orbitalLogistic.file.application.ports.out.InventoryTransactionRepository;
import org.orbitalLogistic.file.application.ports.out.SpacecraftValidationPort;
import org.orbitalLogistic.file.application.ports.out.UserValidationPort;
import org.orbitalLogistic.file.domain.model.InventoryTransaction;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateInventoryTransactionService implements CreateInventoryTransactionUseCase {

    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final CargoValidationPort cargoValidationPort;
    private final SpacecraftValidationPort spacecraftValidationPort;
    private final UserValidationPort userValidationPort;

    @Override
    public InventoryTransaction createTransaction(CreateInventoryTransactionCommand command) {
        log.debug("Creating inventory transaction for cargo: {}", command.cargoId());

        if (!cargoValidationPort.cargoExists(command.cargoId())) {
            throw new IllegalArgumentException("Cargo not found with id: " + command.cargoId());
        }

        if (!userValidationPort.userExists(command.performedByUserId())) {
            throw new IllegalArgumentException("User not found with id: " + command.performedByUserId());
        }

        if (command.fromStorageUnitId() != null && !cargoValidationPort.storageUnitExists(command.fromStorageUnitId())) {
            throw new IllegalArgumentException("Source storage unit not found with id: " + command.fromStorageUnitId());
        }
        if (command.toStorageUnitId() != null && !cargoValidationPort.storageUnitExists(command.toStorageUnitId())) {
            throw new IllegalArgumentException("Target storage unit not found with id: " + command.toStorageUnitId());
        }

        if (command.fromSpacecraftId() != null && !spacecraftValidationPort.spacecraftExists(command.fromSpacecraftId())) {
            throw new IllegalArgumentException("Source spacecraft not found with id: " + command.fromSpacecraftId());
        }
        if (command.toSpacecraftId() != null && !spacecraftValidationPort.spacecraftExists(command.toSpacecraftId())) {
            throw new IllegalArgumentException("Target spacecraft not found with id: " + command.toSpacecraftId());
        }

        InventoryTransaction transaction = InventoryTransaction.builder()
                .transactionType(command.transactionType())
                .cargoId(command.cargoId())
                .quantity(command.quantity())
                .fromStorageUnitId(command.fromStorageUnitId())
                .toStorageUnitId(command.toStorageUnitId())
                .fromSpacecraftId(command.fromSpacecraftId())
                .toSpacecraftId(command.toSpacecraftId())
                .performedByUserId(command.performedByUserId())
                .transactionDate(command.transactionDate() != null ? command.transactionDate() : LocalDateTime.now())
                .reasonCode(command.reasonCode())
                .notes(command.notes())
                .build();

        transaction.validate();

        InventoryTransaction savedTransaction = inventoryTransactionRepository.save(transaction);
        log.info("Created inventory transaction with id: {}", savedTransaction.getId());

        return savedTransaction;
    }
}
