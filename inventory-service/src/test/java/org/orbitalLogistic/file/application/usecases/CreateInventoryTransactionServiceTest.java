package org.orbitalLogistic.file.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.file.application.ports.in.CreateInventoryTransactionCommand;
import org.orbitalLogistic.file.application.ports.out.CargoValidationPort;
import org.orbitalLogistic.file.application.ports.out.InventoryTransactionRepository;
import org.orbitalLogistic.file.application.ports.out.UserValidationPort;
import org.orbitalLogistic.file.domain.model.InventoryTransaction;
import org.orbitalLogistic.file.domain.model.enums.TransactionType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateInventoryTransactionServiceTest {

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private CargoValidationPort cargoValidationPort;

    @Mock
    private UserValidationPort userValidationPort;

    @InjectMocks
    private CreateInventoryTransactionService createInventoryTransactionService;

    private CreateInventoryTransactionCommand command;

    @BeforeEach
    void setUp() {
        command = new CreateInventoryTransactionCommand(
                TransactionType.LOAD,
                1L, // cargoId
                10, // quantity
                null,
                1L, // toStorageUnitId
                null,
                null,
                1L, // performedByUserId
                LocalDateTime.now(),
                "TEST",
                "Test transaction"
        );
    }

    @Test
    @DisplayName("Should create inventory transaction successfully")
    void createTransaction_Success() {
        when(cargoValidationPort.cargoExists(1L)).thenReturn(true);
        when(userValidationPort.userExists(1L)).thenReturn(true);
        when(cargoValidationPort.storageUnitExists(1L)).thenReturn(true);

        InventoryTransaction savedTransaction = InventoryTransaction.builder()
                .id(1L)
                .transactionType(TransactionType.LOAD)
                .cargoId(1L)
                .quantity(10)
                .toStorageUnitId(1L)
                .performedByUserId(1L)
                .transactionDate(command.transactionDate())
                .reasonCode("TEST")
                .notes("Test transaction")
                .build();

        when(inventoryTransactionRepository.save(any(InventoryTransaction.class))).thenReturn(savedTransaction);
        InventoryTransaction result = createInventoryTransactionService.createTransaction(command);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(inventoryTransactionRepository).save(any(InventoryTransaction.class));
    }

    @Test
    @DisplayName("Should throw exception when cargo not found")
    void createTransaction_CargoNotFound() {
        when(cargoValidationPort.cargoExists(1L)).thenReturn(false);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createInventoryTransactionService.createTransaction(command)
        );

        assertEquals("Cargo not found with id: 1", exception.getMessage());
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void createTransaction_UserNotFound() {
        when(cargoValidationPort.cargoExists(1L)).thenReturn(true);
        when(userValidationPort.userExists(1L)).thenReturn(false);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createInventoryTransactionService.createTransaction(command)
        );

        assertEquals("User not found with id: 1", exception.getMessage());
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when storage unit not found")
    void createTransaction_StorageUnitNotFound() {
        when(cargoValidationPort.cargoExists(1L)).thenReturn(true);
        when(userValidationPort.userExists(1L)).thenReturn(true);
        when(cargoValidationPort.storageUnitExists(1L)).thenReturn(false);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createInventoryTransactionService.createTransaction(command)
        );

        assertEquals("Target storage unit not found with id: 1", exception.getMessage());
        verify(inventoryTransactionRepository, never()).save(any());
    }
}
