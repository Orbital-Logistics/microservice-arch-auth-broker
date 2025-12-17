package org.orbitalLogistic.cargo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.controllers.StorageUnitController;
import org.orbitalLogistic.cargo.dto.common.PageResponseDTO;
import org.orbitalLogistic.cargo.dto.request.StorageUnitRequestDTO;
import org.orbitalLogistic.cargo.dto.response.StorageUnitResponseDTO;
import org.orbitalLogistic.cargo.entities.enums.StorageTypeEnum;
import org.orbitalLogistic.cargo.exceptions.StorageUnitNotFoundException;
import org.orbitalLogistic.cargo.services.StorageUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StorageUnitController.class)
class StorageUnitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StorageUnitService storageUnitService;

    private StorageUnitResponseDTO responseDTO;
    private StorageUnitRequestDTO requestDTO;
    private PageResponseDTO<StorageUnitResponseDTO> pageResponse;

    @BeforeEach
    void setUp() {
        responseDTO = new StorageUnitResponseDTO(
                1L,
                "SU-001",
                "Warehouse A",
                StorageTypeEnum.AMBIENT,
                new BigDecimal("10000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("250.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("250.00"),
                50.0,
                50.0
        );

        requestDTO = new StorageUnitRequestDTO(
                "SU-001",
                "Warehouse A",
                StorageTypeEnum.AMBIENT,
                new BigDecimal("10000.00"),
                new BigDecimal("500.00")
        );

        pageResponse = new PageResponseDTO<>(
                List.of(responseDTO),
                0,
                20,
                1L,
                1,
                true,
                true
        );
    }

    @Test
    void getAllStorageUnits_Success() throws Exception {
        when(storageUnitService.getStorageUnits(0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/storage-units?page=0&size=20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].unitCode").value("SU-001"));

        verify(storageUnitService).getStorageUnits(0, 20);
    }

    @Test
    void getStorageUnitById_Success() throws Exception {
        when(storageUnitService.getStorageUnitById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/storage-units/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.unitCode").value("SU-001"));

        verify(storageUnitService).getStorageUnitById(1L);
    }

    @Test
    void getStorageUnitById_NotFound() throws Exception {
        when(storageUnitService.getStorageUnitById(999L))
                .thenThrow(new StorageUnitNotFoundException("Storage unit not found"));

        mockMvc.perform(get("/api/storage-units/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(storageUnitService).getStorageUnitById(999L);
    }

    @Test
    void createStorageUnit_Success() throws Exception {
        when(storageUnitService.createStorageUnit(any(StorageUnitRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/storage-units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.unitCode").value("SU-001"));

        verify(storageUnitService).createStorageUnit(any(StorageUnitRequestDTO.class));
    }

    @Test
    void createStorageUnit_InvalidData() throws Exception {
        StorageUnitRequestDTO invalidRequest = new StorageUnitRequestDTO(
                "",
                "Warehouse A",
                StorageTypeEnum.AMBIENT,
                new BigDecimal("10000.00"),
                new BigDecimal("500.00")
        );

        mockMvc.perform(post("/api/storage-units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(storageUnitService, never()).createStorageUnit(any(StorageUnitRequestDTO.class));
    }

    @Test
    void updateStorageUnit_Success() throws Exception {
        when(storageUnitService.updateStorageUnit(eq(1L), any(StorageUnitRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/api/storage-units/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.unitCode").value("SU-001"));

        verify(storageUnitService).updateStorageUnit(eq(1L), any(StorageUnitRequestDTO.class));
    }

    @Test
    void updateStorageUnit_NotFound() throws Exception {
        when(storageUnitService.updateStorageUnit(eq(999L), any(StorageUnitRequestDTO.class)))
                .thenThrow(new StorageUnitNotFoundException("Storage unit not found"));

        mockMvc.perform(put("/api/storage-units/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());

        verify(storageUnitService).updateStorageUnit(eq(999L), any(StorageUnitRequestDTO.class));
    }

    @Test
    void getStorageUnitInventory_Success() throws Exception {
        PageResponseDTO emptyPage = new PageResponseDTO<>(List.of(), 0, 20, 0L, 0, true, true);
        when(storageUnitService.getStorageUnitInventory(1L, 0, 20)).thenReturn(emptyPage);

        mockMvc.perform(get("/api/storage-units/1/inventory?page=0&size=20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "0"));

        verify(storageUnitService).getStorageUnitInventory(1L, 0, 20);
    }

    @Test
    void storageUnitExists_True() throws Exception {
        when(storageUnitService.storageUnitExists(1L)).thenReturn(true);

        mockMvc.perform(get("/api/storage-units/1/exists")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(storageUnitService).storageUnitExists(1L);
    }

    @Test
    void storageUnitExists_False() throws Exception {
        when(storageUnitService.storageUnitExists(999L)).thenReturn(false);

        mockMvc.perform(get("/api/storage-units/999/exists")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(storageUnitService).storageUnitExists(999L);
    }

    @Test
    void getAllStorageUnits_ExceedsMaxSize() throws Exception {
        when(storageUnitService.getStorageUnits(0, 50)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/storage-units?page=0&size=100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(storageUnitService).getStorageUnits(0, 50);
    }
}

