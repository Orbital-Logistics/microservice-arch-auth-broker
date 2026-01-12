package org.orbitalLogistic.inventory.infrastructure.adapters.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.inventory.domain.model.InventoryTransaction;
import org.orbitalLogistic.inventory.domain.model.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryTransactionPersistenceAdapterTest {

    @Mock
    private InventoryTransactionJdbcRepository inventoryTransactionJdbcRepository;

    @Mock
    private InventoryTransactionPersistenceMapper inventoryTransactionPersistenceMapper;

    @InjectMocks
    private InventoryTransactionPersistenceAdapter inventoryTransactionPersistenceAdapter;

    private InventoryTransaction domainTransaction;
    private InventoryTransactionEntity entity;

    @BeforeEach
    void setUp() {
        domainTransaction = InventoryTransaction.builder()
                .id(1L)
                .transactionType(TransactionType.LOAD)
                .cargoId(1L)
                .quantity(10)
                .toStorageUnitId(1L)
                .performedByUserId(1L)
                .transactionDate(LocalDateTime.now())
                .build();

        entity = InventoryTransactionEntity.builder()
                .id(1L)
                .transactionType("LOAD")
                .cargoId(1L)
                .quantity(10)
                .toStorageUnitId(1L)
                .performedByUserId(1L)
                .transactionDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should save transaction successfully")
    void save_Success() {
        when(inventoryTransactionPersistenceMapper.toEntity(domainTransaction)).thenReturn(entity);
        when(inventoryTransactionJdbcRepository.save(entity)).thenReturn(entity);
        when(inventoryTransactionPersistenceMapper.toDomain(entity)).thenReturn(domainTransaction);
        InventoryTransaction result = inventoryTransactionPersistenceAdapter.save(domainTransaction);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(TransactionType.LOAD, result.getTransactionType());
        verify(inventoryTransactionJdbcRepository).save(entity);
    }

    @Test
    @DisplayName("Should find all transactions")
    void findAll_Success() {
        when(inventoryTransactionJdbcRepository.findAllPaginated(20, 0)).thenReturn(List.of(entity));
        when(inventoryTransactionPersistenceMapper.toDomain(entity)).thenReturn(domainTransaction);
        List<InventoryTransaction> result = inventoryTransactionPersistenceAdapter.findAll(20, 0);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    @Test
    @DisplayName("Should find transactions by cargo id")
    void findByCargoId_Success() {
        when(inventoryTransactionJdbcRepository.findByCargoIdPaginated(1L, 20, 0)).thenReturn(List.of(entity));
        when(inventoryTransactionPersistenceMapper.toDomain(entity)).thenReturn(domainTransaction);
        List<InventoryTransaction> result = inventoryTransactionPersistenceAdapter.findByCargoId(1L, 20, 0);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getCargoId());
    }

    @Test
    @DisplayName("Should count all transactions")
    void countAll_Success() {
        when(inventoryTransactionJdbcRepository.countAll()).thenReturn(10L);
        long result = inventoryTransactionPersistenceAdapter.countAll();
        assertEquals(10L, result);
    }

    @Test
    @DisplayName("Should count transactions by cargo id")
    void countByCargoId_Success() {
        when(inventoryTransactionJdbcRepository.countByCargoId(1L)).thenReturn(5L);
        long result = inventoryTransactionPersistenceAdapter.countByCargoId(1L);
        assertEquals(5L, result);
    }
}
