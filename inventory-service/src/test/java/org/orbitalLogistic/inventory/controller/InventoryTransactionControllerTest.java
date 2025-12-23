package org.orbitalLogistic.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.inventory.controllers.InventoryTransactionController;
import org.orbitalLogistic.inventory.dto.common.PageResponseDTO;
import org.orbitalLogistic.inventory.dto.request.InventoryTransactionRequestDTO;
import org.orbitalLogistic.inventory.dto.response.InventoryTransactionResponseDTO;
import org.orbitalLogistic.inventory.entities.enums.TransactionType;
import org.orbitalLogistic.inventory.services.InventoryTransactionService;
import org.orbitalLogistic.inventory.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryTransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class InventoryTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryTransactionService inventoryTransactionService;

    @MockitoBean
    private JwtService jwtService;

    private InventoryTransactionResponseDTO transactionResponse;
    private InventoryTransactionRequestDTO transactionRequest;
    private LocalDateTime transactionDate;

    @BeforeEach
    void setUp() {
        transactionDate = LocalDateTime.of(2025, 12, 10, 10, 0);

        transactionResponse = new InventoryTransactionResponseDTO(
                1L, TransactionType.LOAD,
                1L, "Test Cargo",
                100,
                1L, "UNIT-001",
                null, null,
                null, null,
                1L, "Star Carrier",
                1L, "John Doe",
                transactionDate, "LOADING", "REF-001", "Test note"
        );

        transactionRequest = new InventoryTransactionRequestDTO(
                TransactionType.LOAD,
                1L, 100,
                1L, null,
                null, 1L,
                1L, transactionDate,
                "LOADING", "REF-001", "Test note"
        );
    }

    @Test
    @DisplayName("GET /api/inventory-transactions - получение всех транзакций")
    void getAllTransactions_Success() throws Exception {
        PageResponseDTO<InventoryTransactionResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(transactionResponse), 0, 20, 1, 1, true, true
        );
        when(inventoryTransactionService.getAllTransactions(0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/inventory-transactions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].transactionType", is("LOAD")))
                .andExpect(jsonPath("$.content[0].quantity", is(100)));

        verify(inventoryTransactionService).getAllTransactions(0, 20);
    }

    @Test
    @DisplayName("GET /api/inventory-transactions - пустой список")
    void getAllTransactions_EmptyList() throws Exception {
        PageResponseDTO<InventoryTransactionResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(), 0, 20, 0, 0, true, true
        );
        when(inventoryTransactionService.getAllTransactions(0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/inventory-transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)));

        verify(inventoryTransactionService).getAllTransactions(0, 20);
    }

    @Test
    @DisplayName("GET /api/inventory-transactions/cargo/{cargoId} - получение по грузу")
    void getTransactionsByCargo_Success() throws Exception {
        PageResponseDTO<InventoryTransactionResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(transactionResponse), 0, 20, 1, 1, true, true
        );
        when(inventoryTransactionService.getTransactionsByCargo(1L, 0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/inventory-transactions/cargo/1")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].cargoId", is(1)));

        verify(inventoryTransactionService).getTransactionsByCargo(1L, 0, 20);
    }

    @Test
    @DisplayName("POST /api/inventory-transactions - создание транзакции")
    void createTransaction_Success() throws Exception {
        when(inventoryTransactionService.createTransaction(any(InventoryTransactionRequestDTO.class)))
                .thenReturn(transactionResponse);

        mockMvc.perform(post("/api/inventory-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.transactionType", is("LOAD")))
                .andExpect(jsonPath("$.quantity", is(100)));

        verify(inventoryTransactionService).createTransaction(any(InventoryTransactionRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/inventory-transactions - невалидные данные (null transactionType)")
    void createTransaction_InvalidData_NullTransactionType() throws Exception {
        String invalidJson = """
                {
                    "transactionType": null,
                    "cargoId": 1,
                    "quantity": 100,
                    "performedByUserId": 1
                }
                """;

        mockMvc.perform(post("/api/inventory-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(inventoryTransactionService, never()).createTransaction(any());
    }

    @Test
    @DisplayName("POST /api/inventory-transactions - невалидные данные (null cargoId)")
    void createTransaction_InvalidData_NullCargoId() throws Exception {
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

        verify(inventoryTransactionService, never()).createTransaction(any());
    }

    @Test
    @DisplayName("POST /api/inventory-transactions - невалидные данные (отрицательное количество)")
    void createTransaction_InvalidData_NegativeQuantity() throws Exception {
        String invalidJson = """
                {
                    "transactionType": "LOAD",
                    "cargoId": 1,
                    "quantity": -10,
                    "performedByUserId": 1
                }
                """;

        mockMvc.perform(post("/api/inventory-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(inventoryTransactionService, never()).createTransaction(any());
    }

    @Test
    @DisplayName("GET /api/inventory-transactions - максимальный размер страницы ограничен")
    void getAllTransactions_MaxSizeLimit() throws Exception {
        PageResponseDTO<InventoryTransactionResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(transactionResponse), 0, 50, 1, 1, true, true
        );
        when(inventoryTransactionService.getAllTransactions(0, 50)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/inventory-transactions")
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk());

        verify(inventoryTransactionService).getAllTransactions(0, 50);
    }
}

