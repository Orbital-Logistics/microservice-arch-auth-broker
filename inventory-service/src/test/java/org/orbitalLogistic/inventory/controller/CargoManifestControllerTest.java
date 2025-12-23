package org.orbitalLogistic.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.inventory.controllers.CargoManifestController;
import org.orbitalLogistic.inventory.dto.common.PageResponseDTO;
import org.orbitalLogistic.inventory.dto.request.CargoManifestRequestDTO;
import org.orbitalLogistic.inventory.dto.response.CargoManifestResponseDTO;
import org.orbitalLogistic.inventory.entities.enums.ManifestPriority;
import org.orbitalLogistic.inventory.entities.enums.ManifestStatus;
import org.orbitalLogistic.inventory.exceptions.CargoManifestNotFoundException;
import org.orbitalLogistic.inventory.services.CargoManifestService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CargoManifestController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class CargoManifestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CargoManifestService cargoManifestService;

    @MockitoBean
    private JwtService jwtService;

    private CargoManifestResponseDTO manifestResponse;
    private CargoManifestRequestDTO manifestRequest;
    private LocalDateTime loadedAt;

    @BeforeEach
    void setUp() {
        loadedAt = LocalDateTime.of(2025, 12, 10, 10, 0);

        manifestResponse = new CargoManifestResponseDTO(
                1L, 1L, "Star Carrier",
                1L, "Test Cargo",
                1L, "UNIT-001",
                100, loadedAt, null,
                1L, "John Doe",
                null, null,
                ManifestStatus.LOADED,
                ManifestPriority.HIGH
        );

        manifestRequest = new CargoManifestRequestDTO(
                1L, 1L, 1L, 100,
                loadedAt, null, 1L, null,
                ManifestStatus.LOADED,
                ManifestPriority.HIGH
        );
    }

    @Test
    @DisplayName("GET /api/cargo-manifests - получение всех манифестов")
    void getAllManifests_Success() throws Exception {
        PageResponseDTO<CargoManifestResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(manifestResponse), 0, 20, 1, 1, true, true
        );
        when(cargoManifestService.getAllManifests(0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/cargo-manifests")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].quantity", is(100)));

        verify(cargoManifestService).getAllManifests(0, 20);
    }

    @Test
    @DisplayName("GET /api/cargo-manifests/spacecraft/{spacecraftId} - получение по кораблю")
    void getManifestsBySpacecraft_Success() throws Exception {
        PageResponseDTO<CargoManifestResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(manifestResponse), 0, 20, 1, 1, true, true
        );
        when(cargoManifestService.getManifestsBySpacecraft(1L, 0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/cargo-manifests/spacecraft/1")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].spacecraftId", is(1)));

        verify(cargoManifestService).getManifestsBySpacecraft(1L, 0, 20);
    }

    @Test
    @DisplayName("GET /api/cargo-manifests/{id} - получение манифеста по ID")
    void getManifestById_Success() throws Exception {
        when(cargoManifestService.getManifestById(1L)).thenReturn(manifestResponse);

        mockMvc.perform(get("/api/cargo-manifests/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.quantity", is(100)))
                .andExpect(jsonPath("$.manifestStatus", is("LOADED")));

        verify(cargoManifestService).getManifestById(1L);
    }

    @Test
    @DisplayName("GET /api/cargo-manifests/{id} - манифест не найден")
    void getManifestById_NotFound() throws Exception {
        when(cargoManifestService.getManifestById(999L))
                .thenThrow(new CargoManifestNotFoundException("Manifest not found"));

        mockMvc.perform(get("/api/cargo-manifests/999"))
                .andExpect(status().isNotFound());

        verify(cargoManifestService).getManifestById(999L);
    }

    @Test
    @DisplayName("POST /api/cargo-manifests - создание манифеста")
    void createManifest_Success() throws Exception {
        when(cargoManifestService.createManifest(any(CargoManifestRequestDTO.class)))
                .thenReturn(manifestResponse);

        mockMvc.perform(post("/api/cargo-manifests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(manifestRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.quantity", is(100)));

        verify(cargoManifestService).createManifest(any(CargoManifestRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/cargo-manifests - невалидные данные (null spacecraftId)")
    void createManifest_InvalidData_NullSpacecraftId() throws Exception {
        String invalidJson = """
                {
                    "spacecraftId": null,
                    "cargoId": 1,
                    "storageUnitId": 1,
                    "quantity": 100,
                    "loadedByUserId": 1
                }
                """;

        mockMvc.perform(post("/api/cargo-manifests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(cargoManifestService, never()).createManifest(any());
    }

    @Test
    @DisplayName("POST /api/cargo-manifests - невалидные данные (отрицательное количество)")
    void createManifest_InvalidData_NegativeQuantity() throws Exception {
        String invalidJson = """
                {
                    "spacecraftId": 1,
                    "cargoId": 1,
                    "storageUnitId": 1,
                    "quantity": -10,
                    "loadedByUserId": 1
                }
                """;

        mockMvc.perform(post("/api/cargo-manifests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(cargoManifestService, never()).createManifest(any());
    }

    @Test
    @DisplayName("PUT /api/cargo-manifests/{id} - обновление манифеста")
    void updateManifest_Success() throws Exception {
        when(cargoManifestService.updateManifest(eq(1L), any(CargoManifestRequestDTO.class)))
                .thenReturn(manifestResponse);

        mockMvc.perform(put("/api/cargo-manifests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(manifestRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)));

        verify(cargoManifestService).updateManifest(eq(1L), any(CargoManifestRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/cargo-manifests/{id} - манифест не найден")
    void updateManifest_NotFound() throws Exception {
        when(cargoManifestService.updateManifest(eq(999L), any(CargoManifestRequestDTO.class)))
                .thenThrow(new CargoManifestNotFoundException("Manifest not found"));

        mockMvc.perform(put("/api/cargo-manifests/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(manifestRequest)))
                .andExpect(status().isNotFound());

        verify(cargoManifestService).updateManifest(eq(999L), any(CargoManifestRequestDTO.class));
    }
}

