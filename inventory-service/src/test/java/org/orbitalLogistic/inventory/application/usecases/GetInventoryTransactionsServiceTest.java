package org.orbitalLogistic.inventory.application.usecases;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.inventory.application.ports.out.InventoryTransactionRepository;
import org.orbitalLogistic.inventory.domain.model.InventoryTransaction;
import org.orbitalLogistic.inventory.domain.model.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetInventoryTransactionsServiceTest {

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @InjectMocks
    private GetInventoryTransactionsService getInventoryTransactionsService;

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

        when(inventoryTransactionRepository.findAll(20, 0)).thenReturn(List.of(transaction));
        List<InventoryTransaction> result = getInventoryTransactionsService.getAllTransactions(0, 20);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
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

        when(inventoryTransactionRepository.findByCargoId(1L, 20, 0)).thenReturn(List.of(transaction));
        List<InventoryTransaction> result = getInventoryTransactionsService.getTransactionsByCargo(1L, 0, 20);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getCargoId());
    }

    @Test
    @DisplayName("Should count all transactions")
    void countAllTransactions_Success() {
        when(inventoryTransactionRepository.countAll()).thenReturn(10L);
        long result = getInventoryTransactionsService.countAllTransactions();
        assertEquals(10L, result);
    }

    @Test
    @DisplayName("Should count transactions by cargo")
    void countTransactionsByCargo_Success() {
        when(inventoryTransactionRepository.countByCargoId(1L)).thenReturn(5L);
        long result = getInventoryTransactionsService.countTransactionsByCargo(1L);
        assertEquals(5L, result);
    }
}
