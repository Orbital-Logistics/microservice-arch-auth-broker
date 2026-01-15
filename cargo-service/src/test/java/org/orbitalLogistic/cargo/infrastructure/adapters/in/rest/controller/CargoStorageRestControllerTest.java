package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.application.ports.in.*;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.orbitalLogistic.cargo.filter.JwtService;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateCargoStorageRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.UpdateInventoryRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response.CargoStorageResponse;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.mapper.CargoStorageRestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CargoStorageRestController.class)
@TestPropertySource(properties = {"spring.cloud.config.enabled=false"})
class CargoStorageRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateCargoStorageUseCase createCargoStorageUseCase;

    @MockitoBean
    private GetCargoStorageUseCase getCargoStorageUseCase;

    @MockitoBean
    private UpdateCargoStorageUseCase updateCargoStorageUseCase;

    @MockitoBean
    private DeleteCargoStorageUseCase deleteCargoStorageUseCase;

    @MockitoBean
    private CargoStorageRestMapper cargoStorageMapper;

    @MockitoBean
    private CargoRepository cargoRepository;

    @MockitoBean
    private StorageUnitRepository storageUnitRepository;

    @MockitoBean
    private JwtService jwtService;

    private CargoStorage cargoStorage;
    private CargoStorageResponse storageResponse;
    private CreateCargoStorageRequest createRequest;

    @BeforeEach
    void setUp() {
        cargoStorage = CargoStorage.builder()
                .id(1L)
                .storageUnitId(1L)
                .cargoId(1L)
                .quantity(10)
                .storedAt(LocalDateTime.now())
                .lastCheckedByUserId(1L)
                .lastInventoryCheck(LocalDateTime.now())
                .build();

        storageResponse = CargoStorageResponse.builder()
                .id(1L)
                .storageUnitId(1L)
                .storageUnitCode("UNIT-001")
                .storageLocation("Warehouse A")
                .cargoId(1L)
                .cargoName("Laptop")
                .quantity(10)
                .storedAt(LocalDateTime.now())
                .lastCheckedByUserId(1L)
                .lastCheckedByUserName("admin")
                .lastInventoryCheck(LocalDateTime.now())
                .build();

        createRequest = CreateCargoStorageRequest.builder()
                .storageUnitId(1L)
                .cargoId(1L)
                .quantity(10)
                .updatedByUserId(1L)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getStorageById_Success() throws Exception {
        // Given
        when(getCargoStorageUseCase.getStorageById(1L)).thenReturn(Optional.of(cargoStorage));
        when(cargoStorageMapper.toResponse(cargoStorage)).thenReturn(storageResponse);

        // When & Then
        mockMvc.perform(get("/api/cargo-storages/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.quantity").value(10));

        verify(getCargoStorageUseCase).getStorageById(1L);
        verify(cargoStorageMapper).toResponse(cargoStorage);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getStorageById_NotFound() throws Exception {
        // Given
        when(getCargoStorageUseCase.getStorageById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/cargo-storages/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(getCargoStorageUseCase).getStorageById(999L);
        verify(cargoStorageMapper, never()).toResponse(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllStorages_Success() throws Exception {
        // Given
        when(getCargoStorageUseCase.getAllStorages(0, 20)).thenReturn(Arrays.asList(cargoStorage));
        when(cargoStorageMapper.toResponse(cargoStorage)).thenReturn(storageResponse);

        // When & Then
        mockMvc.perform(get("/api/cargo-storages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(10));

        verify(getCargoStorageUseCase).getAllStorages(0, 20);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createStorage_Success() throws Exception {
        // Given
        when(cargoStorageMapper.toDomain(any(CreateCargoStorageRequest.class))).thenReturn(cargoStorage);
        when(createCargoStorageUseCase.createStorage(any(CargoStorage.class))).thenReturn(cargoStorage);
        when(cargoStorageMapper.toResponse(cargoStorage)).thenReturn(storageResponse);

        // When & Then
        mockMvc.perform(post("/api/cargo-storages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(10));

        verify(createCargoStorageUseCase).createStorage(any(CargoStorage.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateInventory_Success() throws Exception {
        // Given
        UpdateInventoryRequest updateRequest = UpdateInventoryRequest.builder()
                .newQuantity(20)
                .build();

        when(updateCargoStorageUseCase.updateInventory(anyLong(), anyInt())).thenReturn(cargoStorage);
        when(cargoStorageMapper.toResponse(cargoStorage)).thenReturn(storageResponse);

        // When & Then
        mockMvc.perform(put("/api/cargo-storages/{id}/quantity", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(updateCargoStorageUseCase).updateInventory(1L, 20);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteStorage_Success() throws Exception {
        // Given
        doNothing().when(deleteCargoStorageUseCase).deleteStorage(1L);

        // When & Then
        mockMvc.perform(delete("/api/cargo-storages/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(deleteCargoStorageUseCase).deleteStorage(1L);
    }
}
