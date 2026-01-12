package org.orbitalLogistic.inventory.infrastructure.adapters.in.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.inventory.application.ports.in.CreateInventoryTransactionCommand;
import org.orbitalLogistic.inventory.application.ports.in.CreateInventoryTransactionUseCase;
import org.orbitalLogistic.inventory.application.ports.in.GetInventoryTransactionsUseCase;
import org.orbitalLogistic.inventory.domain.model.InventoryTransaction;
import org.orbitalLogistic.inventory.domain.model.enums.TransactionType;
import org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.InventoryTransactionRequestDTO;
import org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.InventoryTransactionResponseDTO;
import org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.PageResponseDTO;
import org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.mapper.InventoryTransactionRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryTransactionRestControllerTest {

    @Mock
    private CreateInventoryTransactionUseCase createInventoryTransactionUseCase;

    @Mock
    private GetInventoryTransactionsUseCase getInventoryTransactionsUseCase;

    @Mock
    private InventoryTransactionRestMapper inventoryTransactionRestMapper;

    @InjectMocks
    private InventoryTransactionRestController inventoryTransactionRestController;

    @Test
    @DisplayName("Should get all transactions successfully")
    void getAllTransactions_Success() {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .id(1L)
                .transactionType(TransactionType.LOAD)
                .cargoId(1L)
                .quantity(10)
                .toStorageUnitId(1L)
                .performedByUserId(1L)
                .transactionDate(LocalDateTime.now())
                .build();

        InventoryTransactionResponseDTO responseDTO = new InventoryTransactionResponseDTO(
                1L, org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.TransactionType.LOAD,
                1L, "Cargo-1", 10, null, null, 1L, "Unit-1",
                null, null, null, null, 1L, "User-1",
                LocalDateTime.now(), "TEST", "Test transaction"
        );

        when(getInventoryTransactionsUseCase.getAllTransactions(0, 20)).thenReturn(List.of(transaction));
        when(getInventoryTransactionsUseCase.countAllTransactions()).thenReturn(1L);
        when(inventoryTransactionRestMapper.toResponseDTO(transaction)).thenReturn(responseDTO);
        ResponseEntity<PageResponseDTO<InventoryTransactionResponseDTO>> response = 
                inventoryTransactionRestController.getAllTransactions(0, 20);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().content().size());
        assertEquals("1", response.getHeaders().getFirst("X-Total-Count"));
    }

    @Test
    @DisplayName("Should get transactions by cargo successfully")
    void getTransactionsByCargo_Success() {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .id(1L)
                .transactionType(TransactionType.LOAD)
                .cargoId(1L)
                .quantity(10)
                .toStorageUnitId(1L)
                .performedByUserId(1L)
                .transactionDate(LocalDateTime.now())
                .build();

        InventoryTransactionResponseDTO responseDTO = new InventoryTransactionResponseDTO(
                1L, org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.TransactionType.LOAD,
                1L, "Cargo-1", 10, null, null, 1L, "Unit-1",
                null, null, null, null, 1L, "User-1",
                LocalDateTime.now(), "TEST", "Test transaction"
        );

        when(getInventoryTransactionsUseCase.getTransactionsByCargo(1L, 0, 20)).thenReturn(List.of(transaction));
        when(getInventoryTransactionsUseCase.countTransactionsByCargo(1L)).thenReturn(1L);
        when(inventoryTransactionRestMapper.toResponseDTO(transaction)).thenReturn(responseDTO);
        ResponseEntity<PageResponseDTO<InventoryTransactionResponseDTO>> response = 
                inventoryTransactionRestController.getTransactionsByCargo(1L, 0, 20);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().content().size());
    }

    @Test
    @DisplayName("Should create transaction successfully")
    void createTransaction_Success() {
        InventoryTransactionRequestDTO requestDTO = new InventoryTransactionRequestDTO(
                org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.TransactionType.LOAD,
                1L, 10, null, 1L, null, null, 1L, LocalDateTime.now(), "TEST", "Test transaction"
        );

        CreateInventoryTransactionCommand command = new CreateInventoryTransactionCommand(
                TransactionType.LOAD, 1L, 10, null, 1L, null, null, 1L,
                LocalDateTime.now(), "TEST", "Test transaction"
        );

        InventoryTransaction transaction = InventoryTransaction.builder()
                .id(1L)
                .transactionType(TransactionType.LOAD)
                .cargoId(1L)
                .quantity(10)
                .toStorageUnitId(1L)
                .performedByUserId(1L)
                .transactionDate(LocalDateTime.now())
                .reasonCode("TEST")
                .notes("Test transaction")
                .build();

        InventoryTransactionResponseDTO responseDTO = new InventoryTransactionResponseDTO(
                1L, org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.TransactionType.LOAD,
                1L, "Cargo-1", 10, null, null, 1L, "Unit-1",
                null, null, null, null, 1L, "User-1",
                LocalDateTime.now(), "TEST", "Test transaction"
        );

        when(inventoryTransactionRestMapper.toCommand(requestDTO)).thenReturn(command);
        when(createInventoryTransactionUseCase.createTransaction(command)).thenReturn(transaction);
        when(inventoryTransactionRestMapper.toResponseDTO(transaction)).thenReturn(responseDTO);
        ResponseEntity<InventoryTransactionResponseDTO> response = 
                inventoryTransactionRestController.createTransaction(requestDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
    }

    @Test
    @DisplayName("Should limit page size to 50")
    void getAllTransactions_LimitPageSize() {
        when(getInventoryTransactionsUseCase.getAllTransactions(0, 50)).thenReturn(List.of());
        when(getInventoryTransactionsUseCase.countAllTransactions()).thenReturn(0L);
        ResponseEntity<PageResponseDTO<InventoryTransactionResponseDTO>> response = 
                inventoryTransactionRestController.getAllTransactions(0, 100);
        verify(getInventoryTransactionsUseCase).getAllTransactions(0, 50);
    }
}
