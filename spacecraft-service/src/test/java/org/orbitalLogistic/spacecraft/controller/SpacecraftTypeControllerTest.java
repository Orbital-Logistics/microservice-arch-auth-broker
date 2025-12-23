package org.orbitalLogistic.spacecraft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.spacecraft.controllers.SpacecraftTypeController;
import org.orbitalLogistic.spacecraft.dto.request.SpacecraftTypeRequestDTO;
import org.orbitalLogistic.spacecraft.dto.response.SpacecraftTypeResponseDTO;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftClassification;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftTypeNotFoundException;
import org.orbitalLogistic.spacecraft.services.JwtService;
import org.orbitalLogistic.spacecraft.services.SpacecraftTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpacecraftTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
class SpacecraftTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SpacecraftTypeService spacecraftTypeService;

    @MockitoBean
    private JwtService jwtService;

    private SpacecraftTypeResponseDTO spacecraftTypeResponse;
    private SpacecraftTypeRequestDTO spacecraftTypeRequest;

    @BeforeEach
    void setUp() {
        spacecraftTypeResponse = new SpacecraftTypeResponseDTO(
                1L,
                "Cargo Hauler",
                SpacecraftClassification.CARGO_HAULER,
                10
        );

        spacecraftTypeRequest = new SpacecraftTypeRequestDTO(
                "Personnel Transport",
                SpacecraftClassification.PERSONNEL_TRANSPORT,
                50
        );
    }

    @Test
    @DisplayName("GET /api/spacecraft-types - получение всех типов кораблей")
    void getAllSpacecraftTypes_Success() throws Exception {
        List<SpacecraftTypeResponseDTO> types = List.of(spacecraftTypeResponse);
        when(spacecraftTypeService.getAllSpacecraftTypes()).thenReturn(types);

        mockMvc.perform(get("/api/spacecraft-types"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].typeName", is("Cargo Hauler")))
                .andExpect(jsonPath("$[0].classification", is("CARGO_HAULER")))
                .andExpect(jsonPath("$[0].maxCrewCapacity", is(10)));

        verify(spacecraftTypeService).getAllSpacecraftTypes();
    }

    @Test
    @DisplayName("GET /api/spacecraft-types - пустой список")
    void getAllSpacecraftTypes_EmptyList() throws Exception {
        when(spacecraftTypeService.getAllSpacecraftTypes()).thenReturn(List.of());

        mockMvc.perform(get("/api/spacecraft-types"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(spacecraftTypeService).getAllSpacecraftTypes();
    }

    @Test
    @DisplayName("GET /api/spacecraft-types/{id} - получение типа корабля по ID")
    void getSpacecraftTypeById_Success() throws Exception {
        when(spacecraftTypeService.getSpacecraftTypeById(1L)).thenReturn(spacecraftTypeResponse);

        mockMvc.perform(get("/api/spacecraft-types/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.typeName", is("Cargo Hauler")))
                .andExpect(jsonPath("$.classification", is("CARGO_HAULER")))
                .andExpect(jsonPath("$.maxCrewCapacity", is(10)));

        verify(spacecraftTypeService).getSpacecraftTypeById(1L);
    }

    @Test
    @DisplayName("GET /api/spacecraft-types/{id} - тип корабля не найден")
    void getSpacecraftTypeById_NotFound() throws Exception {
        when(spacecraftTypeService.getSpacecraftTypeById(999L))
                .thenThrow(new SpacecraftTypeNotFoundException("Spacecraft type not found with id: 999"));

        mockMvc.perform(get("/api/spacecraft-types/999"))
                .andExpect(status().isNotFound());

        verify(spacecraftTypeService).getSpacecraftTypeById(999L);
    }

    @Test
    @DisplayName("POST /api/spacecraft-types - создание типа корабля")
    void createSpacecraftType_Success() throws Exception {
        SpacecraftTypeResponseDTO newResponse = new SpacecraftTypeResponseDTO(
                2L,
                "Personnel Transport",
                SpacecraftClassification.PERSONNEL_TRANSPORT,
                50
        );

        when(spacecraftTypeService.createSpacecraftType(any(SpacecraftTypeRequestDTO.class)))
                .thenReturn(newResponse);

        mockMvc.perform(post("/api/spacecraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spacecraftTypeRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.typeName", is("Personnel Transport")))
                .andExpect(jsonPath("$.classification", is("PERSONNEL_TRANSPORT")))
                .andExpect(jsonPath("$.maxCrewCapacity", is(50)));

        verify(spacecraftTypeService).createSpacecraftType(any(SpacecraftTypeRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/spacecraft-types - невалидные данные (пустое имя типа)")
    void createSpacecraftType_InvalidData_EmptyTypeName() throws Exception {
        SpacecraftTypeRequestDTO invalidRequest = new SpacecraftTypeRequestDTO(
                "",
                SpacecraftClassification.PERSONNEL_TRANSPORT,
                50
        );

        mockMvc.perform(post("/api/spacecraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(spacecraftTypeService, never()).createSpacecraftType(any());
    }

    @Test
    @DisplayName("POST /api/spacecraft-types - невалидные данные (null classification)")
    void createSpacecraftType_InvalidData_NullClassification() throws Exception {
        String invalidJson = """
                {
                    "typeName": "Test Type",
                    "classification": null,
                    "maxCrewCapacity": 50
                }
                """;

        mockMvc.perform(post("/api/spacecraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(spacecraftTypeService, never()).createSpacecraftType(any());
    }

    @Test
    @DisplayName("POST /api/spacecraft-types - невалидные данные (отрицательная вместимость)")
    void createSpacecraftType_InvalidData_NegativeCapacity() throws Exception {
        SpacecraftTypeRequestDTO invalidRequest = new SpacecraftTypeRequestDTO(
                "Test Type",
                SpacecraftClassification.PERSONNEL_TRANSPORT,
                -10
        );

        mockMvc.perform(post("/api/spacecraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(spacecraftTypeService, never()).createSpacecraftType(any());
    }
}

