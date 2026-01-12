package org.orbitalLogistic.inventory.infrastructure.adapters.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.inventory.domain.model.InventoryTransaction;
import org.orbitalLogistic.inventory.domain.model.enums.TransactionType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTransactionPersistenceMapperTest {

    private InventoryTransactionPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new InventoryTransactionPersistenceMapper();
    }

    @Test
    @DisplayName("Should map domain LOAD transaction to entity")
    void toEntity_LoadTransaction() {
        LocalDateTime now = LocalDateTime.now();
        InventoryTransaction transaction = InventoryTransaction.builder()
                .id(1L)
                .transactionType(TransactionType.LOAD)
                .cargoId(10L)
                .quantity(100)
                .toStorageUnitId(5L)
                .performedByUserId(2L)
                .transactionDate(now)
                .reasonCode("LOADING")
                .notes("Test load")
                .build();
        InventoryTransactionEntity entity = mapper.toEntity(transaction);
        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("LOAD", entity.getTransactionType());
        assertEquals(10L, entity.getCargoId());
        assertEquals(100, entity.getQuantity());
        assertNull(entity.getFromStorageUnitId());
        assertEquals(5L, entity.getToStorageUnitId());
        assertNull(entity.getFromSpacecraftId());
        assertNull(entity.getToSpacecraftId());
        assertEquals(2L, entity.getPerformedByUserId());
        assertEquals(now, entity.getTransactionDate());
        assertEquals("LOADING", entity.getReasonCode());
        assertEquals("Test load", entity.getNotes());
    }

    @Test
    @DisplayName("Should map domain TRANSFER transaction to entity")
    void toEntity_TransferTransaction() {
        LocalDateTime now = LocalDateTime.now();
        InventoryTransaction transaction = InventoryTransaction.builder()
                .id(2L)
                .transactionType(TransactionType.TRANSFER)
                .cargoId(20L)
                .quantity(50)
                .fromStorageUnitId(3L)
                .toStorageUnitId(7L)
                .fromSpacecraftId(1L)
                .toSpacecraftId(2L)
                .performedByUserId(5L)
                .transactionDate(now)
                .build();
        InventoryTransactionEntity entity = mapper.toEntity(transaction);
        assertNotNull(entity);
        assertEquals(2L, entity.getId());
        assertEquals("TRANSFER", entity.getTransactionType());
        assertEquals(20L, entity.getCargoId());
        assertEquals(50, entity.getQuantity());
        assertEquals(3L, entity.getFromStorageUnitId());
        assertEquals(7L, entity.getToStorageUnitId());
        assertEquals(1L, entity.getFromSpacecraftId());
        assertEquals(2L, entity.getToSpacecraftId());
        assertEquals(5L, entity.getPerformedByUserId());
        assertEquals(now, entity.getTransactionDate());
    }

    @Test
    @DisplayName("Should map entity to domain LOAD transaction")
    void toDomain_LoadTransaction() {
        LocalDateTime now = LocalDateTime.now();
        InventoryTransactionEntity entity = InventoryTransactionEntity.builder()
                .id(1L)
                .transactionType("LOAD")
                .cargoId(10L)
                .quantity(100)
                .toStorageUnitId(5L)
                .performedByUserId(2L)
                .transactionDate(now)
                .reasonCode("LOADING")
                .notes("Test load")
                .build();
        InventoryTransaction transaction = mapper.toDomain(entity);
        assertNotNull(transaction);
        assertEquals(1L, transaction.getId());
        assertEquals(TransactionType.LOAD, transaction.getTransactionType());
        assertEquals(10L, transaction.getCargoId());
        assertEquals(100, transaction.getQuantity());
        assertNull(transaction.getFromStorageUnitId());
        assertEquals(5L, transaction.getToStorageUnitId());
        assertNull(transaction.getFromSpacecraftId());
        assertNull(transaction.getToSpacecraftId());
        assertEquals(2L, transaction.getPerformedByUserId());
        assertEquals(now, transaction.getTransactionDate());
        assertEquals("LOADING", transaction.getReasonCode());
        assertEquals("Test load", transaction.getNotes());
    }

    @Test
    @DisplayName("Should map entity to domain UNLOAD transaction")
    void toDomain_UnloadTransaction() {
        LocalDateTime now = LocalDateTime.now();
        InventoryTransactionEntity entity = InventoryTransactionEntity.builder()
                .id(3L)
                .transactionType("UNLOAD")
                .cargoId(15L)
                .quantity(75)
                .fromStorageUnitId(8L)
                .performedByUserId(3L)
                .transactionDate(now)
                .build();
        InventoryTransaction transaction = mapper.toDomain(entity);
        assertNotNull(transaction);
        assertEquals(3L, transaction.getId());
        assertEquals(TransactionType.UNLOAD, transaction.getTransactionType());
        assertEquals(15L, transaction.getCargoId());
        assertEquals(75, transaction.getQuantity());
        assertEquals(8L, transaction.getFromStorageUnitId());
        assertNull(transaction.getToStorageUnitId());
        assertEquals(3L, transaction.getPerformedByUserId());
        assertEquals(now, transaction.getTransactionDate());
    }

    @Test
    @DisplayName("Should handle null transaction type in toEntity")
    void toEntity_NullTransactionType() {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .id(1L)
                .transactionType(null)
                .cargoId(10L)
                .quantity(100)
                .performedByUserId(2L)
                .transactionDate(LocalDateTime.now())
                .build();
        InventoryTransactionEntity entity = mapper.toEntity(transaction);
        assertNotNull(entity);
        assertNull(entity.getTransactionType());
    }

    @Test
    @DisplayName("Should handle null transaction type in toDomain")
    void toDomain_NullTransactionType() {
        InventoryTransactionEntity entity = InventoryTransactionEntity.builder()
                .id(1L)
                .transactionType(null)
                .cargoId(10L)
                .quantity(100)
                .performedByUserId(2L)
                .transactionDate(LocalDateTime.now())
                .build();
        InventoryTransaction transaction = mapper.toDomain(entity);
        assertNotNull(transaction);
        assertNull(transaction.getTransactionType());
    }

    @Test
    @DisplayName("Should map all transaction types correctly")
    void toEntity_AllTransactionTypes() {
        for (TransactionType type : TransactionType.values()) {
            InventoryTransaction transaction = InventoryTransaction.builder()
                    .id(1L)
                    .transactionType(type)
                    .cargoId(10L)
                    .quantity(100)
                    .performedByUserId(2L)
                    .transactionDate(LocalDateTime.now())
                    .build();
            InventoryTransactionEntity entity = mapper.toEntity(transaction);
            assertEquals(type.name(), entity.getTransactionType());
        }
    }

    @Test
    @DisplayName("Should handle optional fields being null")
    void toEntity_OptionalFieldsNull() {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .id(1L)
                .transactionType(TransactionType.LOAD)
                .cargoId(10L)
                .quantity(100)
                .performedByUserId(2L)
                .transactionDate(LocalDateTime.now())
                .reasonCode(null)
                .notes(null)
                .build();
        InventoryTransactionEntity entity = mapper.toEntity(transaction);
        assertNotNull(entity);
        assertNull(entity.getReasonCode());
        assertNull(entity.getNotes());
    }

    @Test
    @DisplayName("Should preserve all spacecraft IDs during mapping")
    void toEntity_SpacecraftIds() {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .id(1L)
                .transactionType(TransactionType.TRANSFER)
                .cargoId(10L)
                .quantity(100)
                .fromSpacecraftId(5L)
                .toSpacecraftId(6L)
                .performedByUserId(2L)
                .transactionDate(LocalDateTime.now())
                .build();
        InventoryTransactionEntity entity = mapper.toEntity(transaction);
        assertEquals(5L, entity.getFromSpacecraftId());
        assertEquals(6L, entity.getToSpacecraftId());
    }

    @Test
    @DisplayName("Should round-trip conversion preserve all data")
    void roundTrip_PreservesData() {
        LocalDateTime now = LocalDateTime.now();
        InventoryTransaction original = InventoryTransaction.builder()
                .id(1L)
                .transactionType(TransactionType.TRANSFER)
                .cargoId(10L)
                .quantity(100)
                .fromStorageUnitId(3L)
                .toStorageUnitId(4L)
                .fromSpacecraftId(5L)
                .toSpacecraftId(6L)
                .performedByUserId(2L)
                .transactionDate(now)
                .reasonCode("TEST")
                .notes("Test notes")
                .build();
        InventoryTransactionEntity entity = mapper.toEntity(original);
        InventoryTransaction result = mapper.toDomain(entity);
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getTransactionType(), result.getTransactionType());
        assertEquals(original.getCargoId(), result.getCargoId());
        assertEquals(original.getQuantity(), result.getQuantity());
        assertEquals(original.getFromStorageUnitId(), result.getFromStorageUnitId());
        assertEquals(original.getToStorageUnitId(), result.getToStorageUnitId());
        assertEquals(original.getFromSpacecraftId(), result.getFromSpacecraftId());
        assertEquals(original.getToSpacecraftId(), result.getToSpacecraftId());
        assertEquals(original.getPerformedByUserId(), result.getPerformedByUserId());
        assertEquals(original.getTransactionDate(), result.getTransactionDate());
        assertEquals(original.getReasonCode(), result.getReasonCode());
        assertEquals(original.getNotes(), result.getNotes());
    }
}
