package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.application.ports.in.*;
import org.orbitalLogistic.cargo.application.ports.out.CargoCategoryRepository;
import org.orbitalLogistic.cargo.application.ports.out.CargoStorageRepository;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;
import org.orbitalLogistic.cargo.filter.JwtService;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateCargoRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.UpdateCargoRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response.CargoResponse;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.mapper.CargoRestMapper;
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

@WebMvcTest(CargoRestController.class)
@TestPropertySource(properties = {"spring.cloud.config.enabled=false"})
class CargoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateCargoUseCase createCargoUseCase;

    @MockitoBean
    private GetCargoUseCase getCargoUseCase;

    @MockitoBean
    private UpdateCargoUseCase updateCargoUseCase;

    @MockitoBean
    private DeleteCargoUseCase deleteCargoUseCase;

    @MockitoBean
    private CargoRestMapper cargoMapper;

    @MockitoBean
    private CargoCategoryRepository cargoCategoryRepository;

    @MockitoBean
    private CargoStorageRepository cargoStorageRepository;

    @MockitoBean
    private JwtService jwtService;

    private Cargo cargo;
    private CargoResponse cargoResponse;
    private CreateCargoRequest createRequest;

    @BeforeEach
    void setUp() {
        cargo = Cargo.builder()
                .id(1L)
                .name("Laptop")
                .cargoCategoryId(1L)
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.LOW)
                .massPerUnit(BigDecimal.valueOf(2.5))
                .volumePerUnit(BigDecimal.valueOf(0.05))
                .build();

        cargoResponse = CargoResponse.builder()
                .id(1L)
                .name("Laptop")
                .cargoCategoryId(1L)
                .cargoCategoryName("Electronics")
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.LOW)
                .massPerUnit(BigDecimal.valueOf(2.5))
                .volumePerUnit(BigDecimal.valueOf(0.05))
                .build();

        createRequest = CreateCargoRequest.builder()
                .name("Laptop")
                .cargoCategoryId(1L)
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.LOW)
                .massPerUnit(BigDecimal.valueOf(2.5))
                .volumePerUnit(BigDecimal.valueOf(0.05))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCargoById_Success() throws Exception {
        // Given
        when(getCargoUseCase.getCargoById(1L)).thenReturn(Optional.of(cargo));
        when(cargoMapper.toResponse(cargo)).thenReturn(cargoResponse);

        // When & Then
        mockMvc.perform(get("/api/cargos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(getCargoUseCase).getCargoById(1L);
        verify(cargoMapper).toResponse(cargo);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCargoById_NotFound() throws Exception {
        // Given
        when(getCargoUseCase.getCargoById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/cargos/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(getCargoUseCase).getCargoById(999L);
        verify(cargoMapper, never()).toResponse(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCargos_Success() throws Exception {
        // Given
        when(getCargoUseCase.getAllCargos(0, 20)).thenReturn(Arrays.asList(cargo));
        when(cargoMapper.toResponse(cargo)).thenReturn(cargoResponse);

        // When & Then
        mockMvc.perform(get("/api/cargos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"));

        verify(getCargoUseCase).getAllCargos(0, 20);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCargo_Success() throws Exception {
        // Given
        when(cargoMapper.toDomain(any(CreateCargoRequest.class))).thenReturn(cargo);
        when(createCargoUseCase.createCargo(any(Cargo.class))).thenReturn(cargo);
        when(cargoMapper.toResponse(cargo)).thenReturn(cargoResponse);

        // When & Then
        mockMvc.perform(post("/api/cargos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(createCargoUseCase).createCargo(any(Cargo.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCargo_Success() throws Exception {
        // Given
        UpdateCargoRequest updateRequest = UpdateCargoRequest.builder()
                .name("Updated Laptop")
                .cargoCategoryId(1L)
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.MEDIUM)
                .massPerUnit(BigDecimal.valueOf(3.0))
                .volumePerUnit(BigDecimal.valueOf(0.06))
                .build();

        when(cargoMapper.toDomain(any(UpdateCargoRequest.class), anyLong())).thenReturn(cargo);
        when(updateCargoUseCase.updateCargo(anyLong(), any(Cargo.class))).thenReturn(cargo);
        when(cargoMapper.toResponse(cargo)).thenReturn(cargoResponse);

        // When & Then
        mockMvc.perform(put("/api/cargos/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(updateCargoUseCase).updateCargo(anyLong(), any(Cargo.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCargo_Success() throws Exception {
        // Given
        doNothing().when(deleteCargoUseCase).deleteCargo(1L);

        // When & Then
        mockMvc.perform(delete("/api/cargos/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(deleteCargoUseCase).deleteCargo(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchCargos_Success() throws Exception {
        // Given
        when(getCargoUseCase.searchCargos("Laptop", CargoType.EQUIPMENT, null, 0, 20))
                .thenReturn(Arrays.asList(cargo));
        when(cargoMapper.toResponse(cargo)).thenReturn(cargoResponse);

        // When & Then
        mockMvc.perform(get("/api/cargos/search")
                        .param("name", "Laptop")
                        .param("cargoType", "EQUIPMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].cargoType").value("EQUIPMENT"));

        verify(getCargoUseCase).searchCargos("Laptop", CargoType.EQUIPMENT, null, 0, 20);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchCargos_WithHazardLevel_Success() throws Exception {
        // Given
        when(getCargoUseCase.searchCargos(null, null, HazardLevel.LOW, 0, 20))
                .thenReturn(Arrays.asList(cargo));
        when(cargoMapper.toResponse(cargo)).thenReturn(cargoResponse);

        // When & Then
        mockMvc.perform(get("/api/cargos/search")
                        .param("hazardLevel", "LOW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hazardLevel").value("LOW"));

        verify(getCargoUseCase).searchCargos(null, null, HazardLevel.LOW, 0, 20);
    }
}
