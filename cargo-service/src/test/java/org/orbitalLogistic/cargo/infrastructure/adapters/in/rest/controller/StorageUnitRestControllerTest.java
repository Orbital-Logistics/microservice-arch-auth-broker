package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.application.ports.in.*;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.orbitalLogistic.cargo.domain.model.enums.StorageTypeEnum;
import org.orbitalLogistic.cargo.filter.JwtService;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateStorageUnitRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.UpdateStorageUnitRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response.StorageUnitResponse;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.mapper.StorageUnitRestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StorageUnitRestController.class)
@TestPropertySource(properties = {"spring.cloud.config.enabled=false"})
class StorageUnitRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateStorageUnitUseCase createStorageUnitUseCase;

    @MockitoBean
    private GetStorageUnitUseCase getStorageUnitUseCase;

    @MockitoBean
    private UpdateStorageUnitUseCase updateStorageUnitUseCase;

    @MockitoBean
    private StorageUnitRestMapper unitMapper;

    @MockitoBean
    private JwtService jwtService;

    private StorageUnit storageUnit;
    private StorageUnitResponse unitResponse;
    private CreateStorageUnitRequest createRequest;

    @BeforeEach
    void setUp() {
        storageUnit = StorageUnit.builder()
                .id(1L)
                .unitCode("UNIT-001")
                .storageType(StorageTypeEnum.AMBIENT)
                .location("Warehouse A")
                .maxMass(BigDecimal.valueOf(1000))
                .maxVolume(BigDecimal.valueOf(50))
                .currentMass(BigDecimal.valueOf(100))
                .currentVolume(BigDecimal.valueOf(10))
                .isActive(true)
                .build();

        unitResponse = StorageUnitResponse.builder()
                .id(1L)
                .unitCode("UNIT-001")
                .storageType(StorageTypeEnum.AMBIENT)
                .location("Warehouse A")
                .totalMassCapacity(BigDecimal.valueOf(1000))
                .totalVolumeCapacity(BigDecimal.valueOf(50))
                .currentMass(BigDecimal.valueOf(100))
                .currentVolume(BigDecimal.valueOf(10))
                .availableMassCapacity(BigDecimal.valueOf(900))
                .availableVolumeCapacity(BigDecimal.valueOf(40))
                .massUsagePercentage(10.0)
                .volumeUsagePercentage(20.0)
                .build();

        createRequest = CreateStorageUnitRequest.builder()
                .unitCode("UNIT-001")
                .storageType(StorageTypeEnum.AMBIENT)
                .location("Warehouse A")
                .totalMassCapacity(BigDecimal.valueOf(1000))
                .totalVolumeCapacity(BigDecimal.valueOf(50))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUnitById_Success() throws Exception {
        // Given
        when(getStorageUnitUseCase.getUnitById(1L)).thenReturn(Optional.of(storageUnit));
        when(unitMapper.toResponse(storageUnit)).thenReturn(unitResponse);

        // When & Then
        mockMvc.perform(get("/api/storage-units/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.unitCode").value("UNIT-001"))
                .andExpect(jsonPath("$.location").value("Warehouse A"));

        verify(getStorageUnitUseCase).getUnitById(1L);
        verify(unitMapper).toResponse(storageUnit);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUnitById_NotFound() throws Exception {
        // Given
        when(getStorageUnitUseCase.getUnitById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/storage-units/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(getStorageUnitUseCase).getUnitById(999L);
        verify(unitMapper, never()).toResponse(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUnits_Success() throws Exception {
        // Given
        when(getStorageUnitUseCase.getAllUnits(0, 20)).thenReturn(Arrays.asList(storageUnit));
        when(unitMapper.toResponse(storageUnit)).thenReturn(unitResponse);

        // When & Then
        mockMvc.perform(get("/api/storage-units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unitCode").value("UNIT-001"));

        verify(getStorageUnitUseCase).getAllUnits(0, 20);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUnit_Success() throws Exception {
        // Given
        when(unitMapper.toDomain(any(CreateStorageUnitRequest.class))).thenReturn(storageUnit);
        when(createStorageUnitUseCase.createUnit(any(StorageUnit.class))).thenReturn(storageUnit);
        when(unitMapper.toResponse(storageUnit)).thenReturn(unitResponse);

        // When & Then
        mockMvc.perform(post("/api/storage-units")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitCode").value("UNIT-001"));

        verify(createStorageUnitUseCase).createUnit(any(StorageUnit.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUnit_Success() throws Exception {
        // Given
        UpdateStorageUnitRequest updateRequest = UpdateStorageUnitRequest.builder()
                .unitCode("UNIT-001")
                .location("Warehouse B")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(BigDecimal.valueOf(1000))
                .totalVolumeCapacity(BigDecimal.valueOf(50))
                .build();

        when(unitMapper.toDomain(any(UpdateStorageUnitRequest.class), anyLong())).thenReturn(storageUnit);
        when(updateStorageUnitUseCase.updateUnit(anyLong(), any(StorageUnit.class))).thenReturn(storageUnit);
        when(unitMapper.toResponse(storageUnit)).thenReturn(unitResponse);

        // When & Then
        mockMvc.perform(put("/api/storage-units/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(updateStorageUnitUseCase).updateUnit(anyLong(), any(StorageUnit.class));
    }
}
