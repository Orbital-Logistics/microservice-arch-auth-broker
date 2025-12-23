package org.orbitalLogistic.cargo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.controllers.CargoController;
import org.orbitalLogistic.cargo.dto.common.PageResponseDTO;
import org.orbitalLogistic.cargo.dto.request.CargoRequestDTO;
import org.orbitalLogistic.cargo.dto.response.CargoResponseDTO;
import org.orbitalLogistic.cargo.entities.enums.CargoType;
import org.orbitalLogistic.cargo.entities.enums.HazardLevel;
import org.orbitalLogistic.cargo.exceptions.CargoNotFoundException;
import org.orbitalLogistic.cargo.services.CargoService;
import org.orbitalLogistic.cargo.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CargoController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class CargoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CargoService cargoService;

    @MockitoBean
    private JwtService jwtService;

    private CargoResponseDTO responseDTO;
    private CargoRequestDTO requestDTO;
    private PageResponseDTO<CargoResponseDTO> pageResponse;

    @BeforeEach
    void setUp() {
        responseDTO = new CargoResponseDTO(
                1L,
                "Microchips",
                "Electronics",
                new BigDecimal("0.50"),
                new BigDecimal("0.01"),
                CargoType.EQUIPMENT,
                HazardLevel.NONE,
                100
        );

        requestDTO = new CargoRequestDTO(
                "Microchips",
                1L,
                new BigDecimal("0.50"),
                new BigDecimal("0.01"),
                CargoType.EQUIPMENT,
                HazardLevel.NONE
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
    void getAllCargos_Success() throws Exception {
        when(cargoService.getCargosScroll(0, 20)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/cargos?page=0&size=20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Microchips"));

        verify(cargoService).getCargosScroll(0, 20);
    }

    @Test
    void getAllCargosPaged_Success() throws Exception {
        when(cargoService.getCargosPaged(null, null, null, 0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/cargos/paged?page=0&size=20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(cargoService).getCargosPaged(null, null, null, 0, 20);
    }

    @Test
    void getCargoById_Success() throws Exception {
        when(cargoService.getCargoById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/cargos/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Microchips"));

        verify(cargoService).getCargoById(1L);
    }

    @Test
    void getCargoById_NotFound() throws Exception {
        when(cargoService.getCargoById(999L)).thenThrow(new CargoNotFoundException("Cargo not found"));

        mockMvc.perform(get("/api/cargos/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(cargoService).getCargoById(999L);
    }

    @Test
    void createCargo_Success() throws Exception {
        when(cargoService.createCargo(any(CargoRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/cargos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Microchips"));

        verify(cargoService).createCargo(any(CargoRequestDTO.class));
    }

    @Test
    void createCargo_InvalidData() throws Exception {
        CargoRequestDTO invalidRequest = new CargoRequestDTO(
                "",
                1L,
                new BigDecimal("0.50"),
                new BigDecimal("0.01"),
                CargoType.EQUIPMENT,
                HazardLevel.NONE
        );

        mockMvc.perform(post("/api/cargos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(cargoService, never()).createCargo(any(CargoRequestDTO.class));
    }

    @Test
    void updateCargo_Success() throws Exception {
        when(cargoService.updateCargo(eq(1L), any(CargoRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/cargos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Microchips"));

        verify(cargoService).updateCargo(eq(1L), any(CargoRequestDTO.class));
    }

    @Test
    void updateCargo_NotFound() throws Exception {
        when(cargoService.updateCargo(eq(999L), any(CargoRequestDTO.class)))
                .thenThrow(new CargoNotFoundException("Cargo not found"));

        mockMvc.perform(put("/api/cargos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());

        verify(cargoService).updateCargo(eq(999L), any(CargoRequestDTO.class));
    }

    @Test
    void deleteCargo_Success() throws Exception {
        doNothing().when(cargoService).deleteCargo(1L);

        mockMvc.perform(delete("/api/cargos/1"))
                .andExpect(status().isNoContent());

        verify(cargoService).deleteCargo(1L);
    }

    @Test
    void deleteCargo_NotFound() throws Exception {
        doThrow(new CargoNotFoundException("Cargo not found")).when(cargoService).deleteCargo(999L);

        mockMvc.perform(delete("/api/cargos/999"))
                .andExpect(status().isNotFound());

        verify(cargoService).deleteCargo(999L);
    }

    @Test
    void searchCargos_Success() throws Exception {
        when(cargoService.searchCargos("Micro", "EQUIPMENT", null, 0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/cargos/search")
                        .param("name", "Micro")
                        .param("cargoType", "EQUIPMENT")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.content[0].name").value("Microchips"));

        verify(cargoService).searchCargos("Micro", "EQUIPMENT", null, 0, 20);
    }

    @Test
    void cargoExists_True() throws Exception {
        when(cargoService.cargoExists(1L)).thenReturn(true);

        mockMvc.perform(get("/api/cargos/1/exists")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(cargoService).cargoExists(1L);
    }

    @Test
    void cargoExists_False() throws Exception {
        when(cargoService.cargoExists(999L)).thenReturn(false);

        mockMvc.perform(get("/api/cargos/999/exists")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(cargoService).cargoExists(999L);
    }

    @Test
    void getAllCargos_ExceedsMaxSize() throws Exception {
        when(cargoService.getCargosScroll(0, 50)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/cargos?page=0&size=100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cargoService).getCargosScroll(0, 50);
    }
}

