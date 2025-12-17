package org.orbitalLogistic.spacecraft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.spacecraft.controllers.SpacecraftController;
import org.orbitalLogistic.spacecraft.dto.common.PageResponseDTO;
import org.orbitalLogistic.spacecraft.dto.request.SpacecraftRequestDTO;
import org.orbitalLogistic.spacecraft.dto.response.SpacecraftResponseDTO;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftClassification;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.spacecraft.services.SpacecraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpacecraftController.class)
class SpacecraftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SpacecraftService spacecraftService;

    private SpacecraftResponseDTO spacecraftResponse;
    private SpacecraftRequestDTO spacecraftRequest;

    @BeforeEach
    void setUp() {
        spacecraftResponse = new SpacecraftResponseDTO(
                1L,
                "SC-001",
                "Star Carrier",
                "Cargo Hauler",
                SpacecraftClassification.CARGO_HAULER,
                new BigDecimal("10000.00"),
                new BigDecimal("5000.00"),
                SpacecraftStatus.DOCKED,
                "Mars Orbit",
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        spacecraftRequest = new SpacecraftRequestDTO(
                "SC-002",
                "Nova Transporter",
                1L,
                new BigDecimal("15000.00"),
                new BigDecimal("7500.00"),
                SpacecraftStatus.DOCKED,
                "Earth Orbit"
        );
    }

    @Test
    @DisplayName("GET /api/spacecrafts - успешное получение списка")
    void getAllSpacecrafts_Success() throws Exception {
        PageResponseDTO<SpacecraftResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(spacecraftResponse),
                0,
                20,
                1L,
                1,
                true,
                true
        );

        when(spacecraftService.getSpacecrafts(any(), any(), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/spacecrafts")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].registryCode").value("SC-001"))
                .andExpect(jsonPath("$.content[0].name").value("Star Carrier"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(spacecraftService).getSpacecrafts(null, null, 0, 20);
    }

    @Test
    @DisplayName("GET /api/spacecrafts - с фильтрами")
    void getAllSpacecrafts_WithFilters() throws Exception {
        PageResponseDTO<SpacecraftResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(spacecraftResponse),
                0,
                20,
                1L,
                1,
                true,
                true
        );

        when(spacecraftService.getSpacecrafts(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/spacecrafts")
                        .param("name", "Star")
                        .param("status", "DOCKED")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Star Carrier"))
                .andExpect(jsonPath("$.content[0].status").value("DOCKED"));

        verify(spacecraftService).getSpacecrafts("Star", "DOCKED", 0, 20);
    }

    @Test
    @DisplayName("GET /api/spacecrafts/{id} - успешное получение по ID")
    void getSpacecraftById_Success() throws Exception {
        when(spacecraftService.getSpacecraftById(1L)).thenReturn(spacecraftResponse);

        mockMvc.perform(get("/api/spacecrafts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.registryCode").value("SC-001"))
                .andExpect(jsonPath("$.name").value("Star Carrier"));

        verify(spacecraftService).getSpacecraftById(1L);
    }

    @Test
    @DisplayName("POST /api/spacecrafts - успешное создание")
    void createSpacecraft_Success() throws Exception {
        when(spacecraftService.createSpacecraft(any(SpacecraftRequestDTO.class)))
                .thenReturn(spacecraftResponse);

        mockMvc.perform(post("/api/spacecrafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spacecraftRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.registryCode").value("SC-001"));

        verify(spacecraftService).createSpacecraft(any(SpacecraftRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/spacecrafts - невалидные данные")
    void createSpacecraft_InvalidData() throws Exception {
        SpacecraftRequestDTO invalidRequest = new SpacecraftRequestDTO(
                "", // пустой registryCode
                "Nova Transporter",
                1L,
                new BigDecimal("15000.00"),
                new BigDecimal("7500.00"),
                SpacecraftStatus.DOCKED,
                "Earth Orbit"
        );

        mockMvc.perform(post("/api/spacecrafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/spacecrafts/{id} - успешное обновление")
    void updateSpacecraft_Success() throws Exception {
        SpacecraftResponseDTO updatedResponse = new SpacecraftResponseDTO(
                1L,
                "SC-001",
                "Updated Name",
                "Cargo Hauler",
                SpacecraftClassification.CARGO_HAULER,
                new BigDecimal("10000.00"),
                new BigDecimal("5000.00"),
                SpacecraftStatus.DOCKED,
                "Mars Orbit",
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(spacecraftService.updateSpacecraft(eq(1L), any(SpacecraftRequestDTO.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/spacecrafts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spacecraftRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(spacecraftService).updateSpacecraft(eq(1L), any(SpacecraftRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/spacecrafts/available - получение доступных кораблей")
    void getAvailableSpacecrafts() throws Exception {
        when(spacecraftService.getAvailableSpacecrafts())
                .thenReturn(List.of(spacecraftResponse));

        mockMvc.perform(get("/api/spacecrafts/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("DOCKED"));

        verify(spacecraftService).getAvailableSpacecrafts();
    }

    @Test
    @DisplayName("PUT /api/spacecrafts/{id}/status - обновление статуса")
    void updateSpacecraftStatus() throws Exception {
        SpacecraftResponseDTO updatedResponse = new SpacecraftResponseDTO(
                1L,
                "SC-001",
                "Star Carrier",
                "Cargo Hauler",
                SpacecraftClassification.CARGO_HAULER,
                new BigDecimal("10000.00"),
                new BigDecimal("5000.00"),
                SpacecraftStatus.IN_TRANSIT,
                "Mars Orbit",
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(spacecraftService.updateSpacecraftStatus(1L, SpacecraftStatus.IN_TRANSIT))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/spacecrafts/1/status")
                        .param("status", "IN_TRANSIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));

        verify(spacecraftService).updateSpacecraftStatus(1L, SpacecraftStatus.IN_TRANSIT);
    }

    @Test
    @DisplayName("GET /api/spacecrafts/{id}/exists - проверка существования")
    void spacecraftExists() throws Exception {
        when(spacecraftService.spacecraftExists(1L)).thenReturn(true);

        mockMvc.perform(get("/api/spacecrafts/1/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(spacecraftService).spacecraftExists(1L);
    }

    @Test
    @DisplayName("GET /api/spacecrafts/scroll - прокрутка списка")
    void getSpacecraftsScroll() throws Exception {
        when(spacecraftService.getSpacecraftsScroll(anyInt(), anyInt()))
                .thenReturn(List.of(spacecraftResponse));

        mockMvc.perform(get("/api/spacecrafts/scroll")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].registryCode").value("SC-001"));

        verify(spacecraftService).getSpacecraftsScroll(0, 20);
    }
}

