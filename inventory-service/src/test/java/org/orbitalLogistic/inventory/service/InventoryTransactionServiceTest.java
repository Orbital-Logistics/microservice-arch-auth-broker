package org.orbitalLogistic.inventory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.inventory.clients.*;
import org.orbitalLogistic.inventory.dto.common.PageResponseDTO;
import org.orbitalLogistic.inventory.dto.request.InventoryTransactionRequestDTO;
import org.orbitalLogistic.inventory.dto.response.InventoryTransactionResponseDTO;
import org.orbitalLogistic.inventory.entities.InventoryTransaction;
import org.orbitalLogistic.inventory.entities.enums.TransactionType;
import org.orbitalLogistic.inventory.exceptions.InvalidOperationException;
import org.orbitalLogistic.inventory.mappers.InventoryTransactionMapper;
import org.orbitalLogistic.inventory.repositories.InventoryTransactionRepository;
import org.orbitalLogistic.inventory.services.InventoryTransactionService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryTransactionServiceTest {

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private InventoryTransactionMapper inventoryTransactionMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private CargoServiceClient cargoServiceClient;

    @Mock
    private SpacecraftServiceClient spacecraftServiceClient;

    @InjectMocks
    private InventoryTransactionService inventoryTransactionService;

    private InventoryTransaction testTransaction;
    private InventoryTransactionRequestDTO transactionRequest;
    private InventoryTransactionResponseDTO transactionResponse;
    private UserDTO testUser;
    private CargoDTO testCargo;
    private SpacecraftDTO testSpacecraft;
    private StorageUnitDTO testStorageUnit;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        testTransaction = InventoryTransaction.builder()
                .id(1L)
                .transactionType(TransactionType.LOAD)
                .cargoId(1L)
                .quantity(100)
                .fromStorageUnitId(1L)
                .toSpacecraftId(1L)
                .performedByUserId(1L)
                .transactionDate(now)
                .reasonCode("LOADING")
                .build();

        transactionRequest = new InventoryTransactionRequestDTO(
                TransactionType.LOAD,
                1L, 100,
                1L, null,
                null, 1L,
                1L, now,
                "LOADING", "REF-001", "Test loading"
        );

        testUser = new UserDTO(1L, "John Doe", "john@example.com");
        testCargo = new CargoDTO(1L, "Test Cargo");
        testSpacecraft = new SpacecraftDTO(1L, "SC-001", "Star Carrier");
        testStorageUnit = new StorageUnitDTO(1L, "UNIT-001", "Section A");

        transactionResponse = new InventoryTransactionResponseDTO(
                1L, TransactionType.LOAD,
                1L, "Test Cargo",
                100,
                1L, "UNIT-001",
                null, null,
                null, null,
                1L, "Star Carrier",
                1L, "John Doe",
                now, "LOADING", "REF-001", "Test loading"
        );
    }

    @Test
    @DisplayName("Получение всех транзакций - успешно")
    void getAllTransactions_Success() {
        List<InventoryTransaction> transactions = List.of(testTransaction);
        when(inventoryTransactionRepository.findAllPaginated(10, 0)).thenReturn(transactions);
        when(inventoryTransactionRepository.countAll()).thenReturn(1L);
        when(cargoServiceClient.getCargoById(anyLong())).thenReturn(testCargo);
        when(cargoServiceClient.getStorageUnitById(anyLong())).thenReturn(testStorageUnit);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(),
                nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class), anyString()))
                .thenReturn(transactionResponse);

        PageResponseDTO<InventoryTransactionResponseDTO> result = inventoryTransactionService.getAllTransactions(0, 10);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(1, result.content().size());

        verify(inventoryTransactionRepository).findAllPaginated(10, 0);
        verify(inventoryTransactionRepository).countAll();
    }

    @Test
    @DisplayName("Получение транзакций по грузу - успешно")
    void getTransactionsByCargo_Success() {
        List<InventoryTransaction> transactions = List.of(testTransaction);
        when(inventoryTransactionRepository.findByCargoIdPaginated(1L, 10, 0)).thenReturn(transactions);
        when(inventoryTransactionRepository.countByCargoId(1L)).thenReturn(1L);
        when(cargoServiceClient.getCargoById(anyLong())).thenReturn(testCargo);
        when(cargoServiceClient.getStorageUnitById(anyLong())).thenReturn(testStorageUnit);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(),
                nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class), anyString()))
                .thenReturn(transactionResponse);

        PageResponseDTO<InventoryTransactionResponseDTO> result = inventoryTransactionService.getTransactionsByCargo(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.totalElements());

        verify(inventoryTransactionRepository).findByCargoIdPaginated(1L, 10, 0);
    }

    @Test
    @DisplayName("Создание транзакции - успешно")
    void createTransaction_Success() {
        when(cargoServiceClient.cargoExists(1L)).thenReturn(true);
        when(userServiceClient.userExists(1L)).thenReturn(true);
        when(cargoServiceClient.storageUnitExists(1L)).thenReturn(true);
        when(spacecraftServiceClient.spacecraftExists(1L)).thenReturn(true);
        when(inventoryTransactionMapper.toEntity(any(InventoryTransactionRequestDTO.class))).thenReturn(testTransaction);
        when(inventoryTransactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);
        when(cargoServiceClient.getCargoById(anyLong())).thenReturn(testCargo);
        when(cargoServiceClient.getStorageUnitById(anyLong())).thenReturn(testStorageUnit);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(),
                nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class), anyString()))
                .thenReturn(transactionResponse);

        InventoryTransactionResponseDTO result = inventoryTransactionService.createTransaction(transactionRequest);

        assertNotNull(result);

        verify(cargoServiceClient).cargoExists(1L);
        verify(userServiceClient).userExists(1L);
        verify(cargoServiceClient).storageUnitExists(1L);
        verify(spacecraftServiceClient).spacecraftExists(1L);
        verify(inventoryTransactionRepository).save(any(InventoryTransaction.class));
    }

    @Test
    @DisplayName("Создание транзакции - груз не найден")
    void createTransaction_CargoNotFound() {
        when(cargoServiceClient.cargoExists(999L)).thenReturn(false);

        InventoryTransactionRequestDTO request = new InventoryTransactionRequestDTO(
                TransactionType.LOAD, 999L, 100,
                1L, null, null, 1L, 1L,
                LocalDateTime.now(), "TEST", "REF", "Note"
        );

        assertThrows(InvalidOperationException.class,
                () -> inventoryTransactionService.createTransaction(request));

        verify(cargoServiceClient).cargoExists(999L);
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание транзакции - пользователь не найден")
    void createTransaction_UserNotFound() {
        when(cargoServiceClient.cargoExists(1L)).thenReturn(true);
        when(userServiceClient.userExists(999L)).thenReturn(false);

        InventoryTransactionRequestDTO request = new InventoryTransactionRequestDTO(
                TransactionType.LOAD, 1L, 100,
                1L, null, null, 1L, 999L,
                LocalDateTime.now(), "TEST", "REF", "Note"
        );

        assertThrows(InvalidOperationException.class,
                () -> inventoryTransactionService.createTransaction(request));

        verify(userServiceClient).userExists(999L);
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание транзакции - склад источник не найден")
    void createTransaction_FromStorageUnitNotFound() {
        when(cargoServiceClient.cargoExists(1L)).thenReturn(true);
        when(userServiceClient.userExists(1L)).thenReturn(true);
        when(cargoServiceClient.storageUnitExists(999L)).thenReturn(false);

        InventoryTransactionRequestDTO request = new InventoryTransactionRequestDTO(
                TransactionType.TRANSFER, 1L, 100,
                999L, 1L, null, null, 1L,
                LocalDateTime.now(), "TEST", "REF", "Note"
        );

        assertThrows(InvalidOperationException.class,
                () -> inventoryTransactionService.createTransaction(request));

        verify(cargoServiceClient).storageUnitExists(999L);
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание транзакции - корабль назначения не найден")
    void createTransaction_ToSpacecraftNotFound() {
        when(cargoServiceClient.cargoExists(1L)).thenReturn(true);
        when(userServiceClient.userExists(1L)).thenReturn(true);
        when(cargoServiceClient.storageUnitExists(1L)).thenReturn(true);
        when(spacecraftServiceClient.spacecraftExists(999L)).thenReturn(false);

        InventoryTransactionRequestDTO request = new InventoryTransactionRequestDTO(
                TransactionType.LOAD, 1L, 100,
                1L, null, null, 999L, 1L,
                LocalDateTime.now(), "TEST", "REF", "Note"
        );

        assertThrows(InvalidOperationException.class,
                () -> inventoryTransactionService.createTransaction(request));

        verify(spacecraftServiceClient).spacecraftExists(999L);
        verify(inventoryTransactionRepository, never()).save(any());
    }
}

