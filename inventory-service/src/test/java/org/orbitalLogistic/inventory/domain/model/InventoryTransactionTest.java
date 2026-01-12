package org.orbitalLogistic.inventory.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.inventory.domain.model.enums.TransactionType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTransactionTest {

    @Test
    @DisplayName("Should validate LOAD transaction successfully")
    void validate_LoadTransaction_Success() {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .transactionType(TransactionType.LOAD)
                .cargoId(1L)
                .quantity(10)
                .toStorageUnitId(1L)
                .performedByUserId(1L)
                .transactionDate(LocalDateTime.now())
                .build();
        assertDoesNotThrow(transaction::validate);
    }

    @Test
    @DisplayName("Should throw exception when transaction type is null")
    void validate_NullTransactionType() {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .transactionType(null)
                .cargoId(1L)
                .quantity(10)
                .performedByUserId(1L)
                .build();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                transaction::validate
        );
        assertEquals("Transaction type is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when cargo ID is null")
    void validate_NullCargoId() {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .transactionType(TransactionType.LOAD)
                .cargoId(null)
                .quantity(10)
                .toStorageUnitId(1L)
                .performedByUserId(1L)
                .build();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                transaction::validate
        );
        assertEquals("Cargo ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when quantity is not positive")
    void validate_NonPositiveQuantity() {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .transactionType(TransactionType.LOAD)
                .cargoId(1L)
                .quantity(0)
                .toStorageUnitId(1L)
                .performedByUserId(1L)
                .build();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                transaction::validate
        );
        assertEquals("Quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when LOAD has no target")
    void validate_LoadWithoutTarget() {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .transactionType(TransactionType.LOAD)
                .cargoId(1L)
                .quantity(10)
                .performedByUserId(1L)
                .build();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                transaction::validate
        );
        assertEquals("LOAD transaction requires a target (storage unit or spacecraft)", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when UNLOAD has no source")
    void validate_UnloadWithoutSource() {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .transactionType(TransactionType.UNLOAD)
                .cargoId(1L)
                .quantity(10)
                .performedByUserId(1L)
                .build();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                transaction::validate
        );
        assertEquals("UNLOAD transaction requires a source (storage unit or spacecraft)", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when TRANSFER has no source or target")
    void validate_TransferWithoutSourceOrTarget() {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .transactionType(TransactionType.TRANSFER)
                .cargoId(1L)
                .quantity(10)
                .performedByUserId(1L)
                .fromStorageUnitId(1L)
                .build();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                transaction::validate
        );
        assertEquals("TRANSFER transaction requires both source and target", exception.getMessage());
    }

    @Test
    @DisplayName("Should return true for isLoad")
    void isLoad_True() {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .transactionType(TransactionType.LOAD)
                .build();
        assertTrue(transaction.isLoad());
    }

    @Test
    @DisplayName("Should return true for involvesSpacecraft")
    void involvesSpacecraft_True() {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .fromSpacecraftId(1L)
                .build();
        assertTrue(transaction.involvesSpacecraft());
    }
}
