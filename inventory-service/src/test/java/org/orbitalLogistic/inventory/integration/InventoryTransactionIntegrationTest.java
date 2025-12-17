package org.orbitalLogistic.inventory.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.inventory.TestcontainersConfiguration;
import org.orbitalLogistic.inventory.clients.CargoServiceClient;
import org.orbitalLogistic.inventory.clients.SpacecraftServiceClient;
import org.orbitalLogistic.inventory.clients.UserServiceClient;
import org.orbitalLogistic.inventory.dto.request.InventoryTransactionRequestDTO;
import org.orbitalLogistic.inventory.entities.enums.TransactionType;
import org.orbitalLogistic.inventory.repositories.InventoryTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Tag("integration-tests")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class InventoryTransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @MockitoBean
    private UserServiceClient userServiceClient;

    @MockitoBean
    private CargoServiceClient cargoServiceClient;

    @MockitoBean
    private SpacecraftServiceClient spacecraftServiceClient;

    @BeforeEach
    void setUp() {
        inventoryTransactionRepository.deleteAll();

        when(userServiceClient.userExists(anyLong())).thenReturn(true);
        when(cargoServiceClient.cargoExists(anyLong())).thenReturn(true);
        when(cargoServiceClient.storageUnitExists(anyLong())).thenReturn(true);
        when(spacecraftServiceClient.spacecraftExists(anyLong())).thenReturn(true);
    }

    @Test
    @DisplayName("Интеграционный тест: создание и получение транзакции")
    void inventoryTransactionLifecycle_Integration() throws Exception {
        LocalDateTime transactionDate = LocalDateTime.of(2025, 12, 10, 10, 0);

        InventoryTransactionRequestDTO request = new InventoryTransactionRequestDTO(
                TransactionType.LOAD,
                1L, 100,
                1L, null,
                null, 1L,
                1L, transactionDate,
                "LOADING", "REF-001", "Loading cargo"
        );

        String createdResponse = mockMvc.perform(post("/api/inventory-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType", is("LOAD")))
                .andExpect(jsonPath("$.cargoId", is(1)))
                .andExpect(jsonPath("$.quantity", is(100)))
                .andExpect(jsonPath("$.reasonCode", is("LOADING")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long transactionId = objectMapper.readTree(createdResponse).get("id").asLong();

        mockMvc.perform(get("/api/inventory-transactions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(transactionId.intValue())))
                .andExpect(jsonPath("$.content[0].transactionType", is("LOAD")));
    }

    @Test
    @DisplayName("Интеграционный тест: получение транзакций по грузу")
    void getTransactionsByCargo_Integration() throws Exception {
        LocalDateTime transactionDate = LocalDateTime.now();

        InventoryTransactionRequestDTO transaction1 = new InventoryTransactionRequestDTO(
                TransactionType.LOAD, 1L, 100,
                1L, null, null, 1L, 1L,
                transactionDate, "LOADING", "REF-001", "Load 1"
        );

        InventoryTransactionRequestDTO transaction2 = new InventoryTransactionRequestDTO(
                TransactionType.UNLOAD, 1L, 50,
                null, 1L, 1L, null, 1L,
                transactionDate.plusHours(1), "UNLOADING", "REF-002", "Unload 1"
        );

        InventoryTransactionRequestDTO transaction3 = new InventoryTransactionRequestDTO(
                TransactionType.LOAD, 2L, 200,
                2L, null, null, 2L, 1L,
                transactionDate.plusHours(2), "LOADING", "REF-003", "Load 2"
        );

        mockMvc.perform(post("/api/inventory-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/inventory-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/inventory-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction3)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/inventory-transactions/cargo/1")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));

        mockMvc.perform(get("/api/inventory-transactions/cargo/2")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Интеграционный тест: различные типы транзакций")
    void differentTransactionTypes_Integration() throws Exception {
        LocalDateTime transactionDate = LocalDateTime.now();

        InventoryTransactionRequestDTO loadTransaction = new InventoryTransactionRequestDTO(
                TransactionType.LOAD, 1L, 100,
                1L, null, null, 1L, 1L,
                transactionDate, "LOADING", "LOAD-001", "Loading"
        );

        InventoryTransactionRequestDTO unloadTransaction = new InventoryTransactionRequestDTO(
                TransactionType.UNLOAD, 1L, 50,
                null, 1L, 1L, null, 1L,
                transactionDate.plusHours(1), "UNLOADING", "UNLOAD-001", "Unloading"
        );

        InventoryTransactionRequestDTO transferTransaction = new InventoryTransactionRequestDTO(
                TransactionType.TRANSFER, 1L, 25,
                1L, 2L, null, null, 1L,
                transactionDate.plusHours(2), "TRANSFER", "TRANS-001", "Transfer"
        );

        InventoryTransactionRequestDTO adjustmentTransaction = new InventoryTransactionRequestDTO(
                TransactionType.ADJUSTMENT, 1L, 10,
                1L, null, null, null, 1L,
                transactionDate.plusHours(3), "ADJUSTMENT", "ADJ-001", "Adjustment"
        );

        mockMvc.perform(post("/api/inventory-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadTransaction)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType", is("LOAD")));

        mockMvc.perform(post("/api/inventory-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unloadTransaction)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType", is("UNLOAD")));

        mockMvc.perform(post("/api/inventory-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferTransaction)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType", is("TRANSFER")));

        mockMvc.perform(post("/api/inventory-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjustmentTransaction)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType", is("ADJUSTMENT")));

        mockMvc.perform(get("/api/inventory-transactions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.totalElements", is(4)));
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - груз не найден")
    void createTransaction_CargoNotFound_ReturnsBadRequest() throws Exception {
        when(cargoServiceClient.cargoExists(999L)).thenReturn(false);

        InventoryTransactionRequestDTO request = new InventoryTransactionRequestDTO(
                TransactionType.LOAD, 999L, 100,
                1L, null, null, 1L, 1L,
                LocalDateTime.now(), "LOADING", "REF-001", "Note"
        );

        mockMvc.perform(post("/api/inventory-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - невалидные данные")
    void createTransaction_InvalidData_ReturnsBadRequest() throws Exception {
        String invalidJson = """
                {
                    "transactionType": "LOAD",
                    "cargoId": null,
                    "quantity": 100,
                    "performedByUserId": 1
                }
                """;

        mockMvc.perform(post("/api/inventory-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Интеграционный тест: пагинация работает корректно")
    void pagination_WorksCorrectly() throws Exception {
        LocalDateTime transactionDate = LocalDateTime.now();

        for (int i = 1; i <= 5; i++) {
            InventoryTransactionRequestDTO request = new InventoryTransactionRequestDTO(
                    TransactionType.LOAD, (long) i, 100 * i,
                    (long) i, null, null, 1L, 1L,
                    transactionDate.plusHours(i), "LOADING", "REF-" + i, "Transaction " + i
            );

            mockMvc.perform(post("/api/inventory-transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/inventory-transactions")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.totalPages", is(3)));

        mockMvc.perform(get("/api/inventory-transactions")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        mockMvc.perform(get("/api/inventory-transactions")
                        .param("page", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }
}

