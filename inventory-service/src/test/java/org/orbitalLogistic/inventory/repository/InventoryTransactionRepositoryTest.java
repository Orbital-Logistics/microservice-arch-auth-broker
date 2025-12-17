package org.orbitalLogistic.inventory.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.inventory.TestcontainersConfiguration;
import org.orbitalLogistic.inventory.entities.InventoryTransaction;
import org.orbitalLogistic.inventory.entities.enums.TransactionType;
import org.orbitalLogistic.inventory.repositories.InventoryTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Tag("integration-tests")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class InventoryTransactionRepositoryTest {

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    private InventoryTransaction testTransaction1;
    private InventoryTransaction testTransaction2;

    @BeforeEach
    void setUp() {
        inventoryTransactionRepository.deleteAll();

        LocalDateTime now = LocalDateTime.now();

        testTransaction1 = InventoryTransaction.builder()
                .transactionType(TransactionType.LOAD)
                .cargoId(1L)
                .quantity(100)
                .fromStorageUnitId(1L)
                .toSpacecraftId(1L)
                .performedByUserId(1L)
                .transactionDate(now.minusDays(2))
                .reasonCode("LOADING")
                .build();
        testTransaction1 = inventoryTransactionRepository.save(testTransaction1);

        testTransaction2 = InventoryTransaction.builder()
                .transactionType(TransactionType.UNLOAD)
                .cargoId(2L)
                .quantity(50)
                .fromSpacecraftId(2L)
                .toStorageUnitId(2L)
                .performedByUserId(2L)
                .transactionDate(now.minusDays(1))
                .reasonCode("UNLOADING")
                .build();
        testTransaction2 = inventoryTransactionRepository.save(testTransaction2);
    }

    @Test
    @DisplayName("Поиск транзакции по ID - успешно")
    void findById_Success() {
        Optional<InventoryTransaction> result = inventoryTransactionRepository.findById(testTransaction1.getId());

        assertTrue(result.isPresent());
        assertEquals(TransactionType.LOAD, result.get().getTransactionType());
        assertEquals(1L, result.get().getCargoId());
        assertEquals(100, result.get().getQuantity());
    }

    @Test
    @DisplayName("Поиск транзакции по ID - не найдена")
    void findById_NotFound() {
        Optional<InventoryTransaction> result = inventoryTransactionRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Поиск транзакций по грузу")
    void findByCargoId() {
        List<InventoryTransaction> result = inventoryTransactionRepository.findByCargoId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTransaction1.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Поиск транзакций по грузу - пустой результат")
    void findByCargoId_Empty() {
        List<InventoryTransaction> result = inventoryTransactionRepository.findByCargoId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Получение всех транзакций с пагинацией")
    void findAllPaginated() {
        List<InventoryTransaction> result = inventoryTransactionRepository.findAllPaginated(10, 0);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Получение всех транзакций с пагинацией - вторая страница")
    void findAllPaginated_SecondPage() {
        List<InventoryTransaction> page1 = inventoryTransactionRepository.findAllPaginated(1, 0);
        List<InventoryTransaction> page2 = inventoryTransactionRepository.findAllPaginated(1, 1);

        assertNotNull(page1);
        assertNotNull(page2);
        assertEquals(1, page1.size());
        assertEquals(1, page2.size());
        assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
    }

    @Test
    @DisplayName("Поиск по грузу с пагинацией")
    void findByCargoIdPaginated() {
        List<InventoryTransaction> result = inventoryTransactionRepository.findByCargoIdPaginated(1L, 10, 0);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTransaction1.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Подсчет всех транзакций")
    void countAll() {
        long count = inventoryTransactionRepository.countAll();

        assertEquals(2, count);
    }

    @Test
    @DisplayName("Подсчет по грузу")
    void countByCargoId() {
        long count = inventoryTransactionRepository.countByCargoId(1L);

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Подсчет по грузу - ноль")
    void countByCargoId_Zero() {
        long count = inventoryTransactionRepository.countByCargoId(999L);

        assertEquals(0, count);
    }

    @Test
    @DisplayName("Сохранение транзакции")
    void save_Success() {
        InventoryTransaction newTransaction = InventoryTransaction.builder()
                .transactionType(TransactionType.TRANSFER)
                .cargoId(3L)
                .quantity(75)
                .fromStorageUnitId(1L)
                .toStorageUnitId(2L)
                .performedByUserId(3L)
                .transactionDate(LocalDateTime.now())
                .reasonCode("TRANSFER")
                .build();

        InventoryTransaction saved = inventoryTransactionRepository.save(newTransaction);

        assertNotNull(saved.getId());
        assertEquals(TransactionType.TRANSFER, saved.getTransactionType());
        assertEquals(3L, saved.getCargoId());
        assertEquals(75, saved.getQuantity());
    }

    @Test
    @DisplayName("Удаление транзакции")
    void delete_Success() {
        Long transactionId = testTransaction1.getId();
        assertTrue(inventoryTransactionRepository.existsById(transactionId));

        inventoryTransactionRepository.deleteById(transactionId);

        assertFalse(inventoryTransactionRepository.existsById(transactionId));
    }

    @Test
    @DisplayName("Обновление транзакции")
    void update_Success() {
        testTransaction1.setQuantity(150);
        testTransaction1.setReasonCode("UPDATED");

        InventoryTransaction updated = inventoryTransactionRepository.save(testTransaction1);

        assertEquals(150, updated.getQuantity());
        assertEquals("UPDATED", updated.getReasonCode());
    }
}

