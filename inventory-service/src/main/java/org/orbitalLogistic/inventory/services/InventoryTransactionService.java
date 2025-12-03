package org.orbitalLogistic.inventory.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.inventory.clients.*;
import org.orbitalLogistic.inventory.dto.common.PageResponseDTO;
import org.orbitalLogistic.inventory.dto.request.InventoryTransactionRequestDTO;
import org.orbitalLogistic.inventory.dto.response.InventoryTransactionResponseDTO;
import org.orbitalLogistic.inventory.entities.InventoryTransaction;
import org.orbitalLogistic.inventory.exceptions.InvalidOperationException;
import org.orbitalLogistic.inventory.mappers.InventoryTransactionMapper;
import org.orbitalLogistic.inventory.repositories.InventoryTransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryTransactionService {

    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final UserServiceClient userServiceClient;
    private final CargoServiceClient cargoServiceClient;
    private final SpacecraftServiceClient spacecraftServiceClient;

    public PageResponseDTO<InventoryTransactionResponseDTO> getAllTransactions(int page, int size) {
        int offset = page * size;
        List<InventoryTransaction> transactions = inventoryTransactionRepository.findAllPaginated(size, offset);
        long total = inventoryTransactionRepository.countAll();

        List<InventoryTransactionResponseDTO> transactionDTOs = transactions.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(transactionDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public PageResponseDTO<InventoryTransactionResponseDTO> getTransactionsByCargo(Long cargoId, int page, int size) {
        int offset = page * size;
        List<InventoryTransaction> transactions = inventoryTransactionRepository.findByCargoIdPaginated(cargoId, size, offset);
        long total = inventoryTransactionRepository.countByCargoId(cargoId);

        List<InventoryTransactionResponseDTO> transactionDTOs = transactions.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(transactionDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public InventoryTransactionResponseDTO createTransaction(InventoryTransactionRequestDTO request) {
        // Валидация cargo через Feign Client
        validateCargo(request.cargoId());

        // Валидация performed by user через Feign Client
        validateUser(request.performedByUserId(), "Performed by user");

        // Валидация from storage unit если указан
        if (request.fromStorageUnitId() != null) {
            validateStorageUnit(request.fromStorageUnitId(), "From storage unit");
        }

        // Валидация to storage unit если указан
        if (request.toStorageUnitId() != null) {
            validateStorageUnit(request.toStorageUnitId(), "To storage unit");
        }

        // Валидация from spacecraft если указан
        if (request.fromSpacecraftId() != null) {
            validateSpacecraft(request.fromSpacecraftId(), "From spacecraft");
        }

        // Валидация to spacecraft если указан
        if (request.toSpacecraftId() != null) {
            validateSpacecraft(request.toSpacecraftId(), "To spacecraft");
        }

        InventoryTransaction transaction = inventoryTransactionMapper.toEntity(request);

        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(java.time.LocalDateTime.now());
        }

        InventoryTransaction saved = inventoryTransactionRepository.save(transaction);
        return toResponseDTO(saved);
    }

    private void validateCargo(Long cargoId) {
        try {
            Boolean cargoExists = cargoServiceClient.cargoExists(cargoId);
            if (cargoExists == null || !cargoExists) {
                throw new InvalidOperationException("Cargo not found with id: " + cargoId);
            }
        } catch (Exception e) {
            log.error("Failed to validate cargo: {}", e.getMessage());
            throw new InvalidOperationException("Unable to validate cargo. Cargo service may be unavailable.");
        }
    }

    private void validateUser(Long userId, String fieldName) {
        try {
            Boolean userExists = userServiceClient.userExists(userId);
            if (userExists == null || !userExists) {
                throw new InvalidOperationException(fieldName + " not found with id: " + userId);
            }
        } catch (Exception e) {
            log.error("Failed to validate user: {}", e.getMessage());
            throw new InvalidOperationException("Unable to validate " + fieldName.toLowerCase() + ". User service may be unavailable.");
        }
    }

    private void validateStorageUnit(Long storageUnitId, String fieldName) {
        try {
            Boolean storageUnitExists = cargoServiceClient.storageUnitExists(storageUnitId);
            if (storageUnitExists == null || !storageUnitExists) {
                throw new InvalidOperationException(fieldName + " not found with id: " + storageUnitId);
            }
        } catch (Exception e) {
            log.error("Failed to validate storage unit: {}", e.getMessage());
            throw new InvalidOperationException("Unable to validate " + fieldName.toLowerCase() + ". Cargo service may be unavailable.");
        }
    }

    private void validateSpacecraft(Long spacecraftId, String fieldName) {
        try {
            Boolean spacecraftExists = spacecraftServiceClient.spacecraftExists(spacecraftId);
            if (spacecraftExists == null || !spacecraftExists) {
                throw new InvalidOperationException(fieldName + " not found with id: " + spacecraftId);
            }
        } catch (Exception e) {
            log.error("Failed to validate spacecraft: {}", e.getMessage());
            throw new InvalidOperationException("Unable to validate " + fieldName.toLowerCase() + ". Spacecraft service may be unavailable.");
        }
    }

    private InventoryTransactionResponseDTO toResponseDTO(InventoryTransaction transaction) {
        String cargoName = "Unknown";
        String fromStorageUnitCode = null;
        String toStorageUnitCode = null;
        String fromSpacecraftName = null;
        String toSpacecraftName = null;
        String performedByUserName = "Unknown";

        try {
            CargoDTO cargo = cargoServiceClient.getCargoById(transaction.getCargoId());
            if (cargo != null) {
                cargoName = cargo.name();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch cargo name: {}", e.getMessage());
        }

        if (transaction.getFromStorageUnitId() != null) {
            try {
                StorageUnitDTO storageUnit = cargoServiceClient.getStorageUnitById(transaction.getFromStorageUnitId());
                if (storageUnit != null) {
                    fromStorageUnitCode = storageUnit.unitCode();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch from storage unit: {}", e.getMessage());
            }
        }

        if (transaction.getToStorageUnitId() != null) {
            try {
                StorageUnitDTO storageUnit = cargoServiceClient.getStorageUnitById(transaction.getToStorageUnitId());
                if (storageUnit != null) {
                    toStorageUnitCode = storageUnit.unitCode();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch to storage unit: {}", e.getMessage());
            }
        }

        if (transaction.getFromSpacecraftId() != null) {
            try {
                SpacecraftDTO spacecraft = spacecraftServiceClient.getSpacecraftById(transaction.getFromSpacecraftId());
                if (spacecraft != null) {
                    fromSpacecraftName = spacecraft.name();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch from spacecraft: {}", e.getMessage());
            }
        }

        if (transaction.getToSpacecraftId() != null) {
            try {
                SpacecraftDTO spacecraft = spacecraftServiceClient.getSpacecraftById(transaction.getToSpacecraftId());
                if (spacecraft != null) {
                    toSpacecraftName = spacecraft.name();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch to spacecraft: {}", e.getMessage());
            }
        }

        try {
            UserDTO user = userServiceClient.getUserById(transaction.getPerformedByUserId());
            if (user != null) {
                performedByUserName = user.username();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch performed by user: {}", e.getMessage());
        }

        return inventoryTransactionMapper.toResponseDTO(
            transaction,
            cargoName,
            fromStorageUnitCode,
            toStorageUnitCode,
            fromSpacecraftName,
            toSpacecraftName,
            performedByUserName
        );
    }
}

