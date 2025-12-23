package org.orbitalLogistic.mission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.mission.controllers.SpacecraftMissionController;
import org.orbitalLogistic.mission.dto.request.SpacecraftMissionRequestDTO;
import org.orbitalLogistic.mission.dto.response.SpacecraftMissionResponseDTO;
import org.orbitalLogistic.mission.exceptions.MissionNotFoundException;
import org.orbitalLogistic.mission.exceptions.MissionSpacecraftExistsException;
import org.orbitalLogistic.mission.services.JwtService;
import org.orbitalLogistic.mission.services.SpacecraftMissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpacecraftMissionController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class SpacecraftMissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SpacecraftMissionService spacecraftMissionService;

    @MockitoBean
    private JwtService jwtService;

    private SpacecraftMissionResponseDTO spacecraftMissionResponse;
    private SpacecraftMissionRequestDTO spacecraftMissionRequest;

    @BeforeEach
    void setUp() {
        spacecraftMissionResponse = new SpacecraftMissionResponseDTO(
                1L,
                1L,
                "Star Carrier",
                1L,
                "Test Mission"
        );

        spacecraftMissionRequest = new SpacecraftMissionRequestDTO(1L, 1L);
    }

    @Test
    @DisplayName("GET /api/spacecraft-missions - получение всех назначений")
    void getAllSpacecraftMissions_Success() throws Exception {
        when(spacecraftMissionService.getAllSpacecraftMissions())
                .thenReturn(List.of(spacecraftMissionResponse));

        mockMvc.perform(get("/api/spacecraft-missions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].spacecraftName", is("Star Carrier")))
                .andExpect(jsonPath("$[0].missionName", is("Test Mission")));

        verify(spacecraftMissionService).getAllSpacecraftMissions();
    }

    @Test
    @DisplayName("GET /api/spacecraft-missions - пустой список")
    void getAllSpacecraftMissions_EmptyList() throws Exception {
        when(spacecraftMissionService.getAllSpacecraftMissions()).thenReturn(List.of());

        mockMvc.perform(get("/api/spacecraft-missions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(spacecraftMissionService).getAllSpacecraftMissions();
    }

    @Test
    @DisplayName("GET /api/spacecraft-missions/spacecraft/{spacecraftId} - получение по кораблю")
    void getBySpacecraft_Success() throws Exception {
        when(spacecraftMissionService.getBySpacecraft(1L))
                .thenReturn(List.of(spacecraftMissionResponse));

        mockMvc.perform(get("/api/spacecraft-missions/spacecraft/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].spacecraftId", is(1)));

        verify(spacecraftMissionService).getBySpacecraft(1L);
    }

    @Test
    @DisplayName("GET /api/spacecraft-missions/mission/{missionId} - получение по миссии")
    void getByMission_Success() throws Exception {
        when(spacecraftMissionService.getByMission(1L))
                .thenReturn(List.of(spacecraftMissionResponse));

        mockMvc.perform(get("/api/spacecraft-missions/mission/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].missionId", is(1)));

        verify(spacecraftMissionService).getByMission(1L);
    }

    @Test
    @DisplayName("POST /api/spacecraft-missions - создание назначения")
    void createSpacecraftMission_Success() throws Exception {
        when(spacecraftMissionService.createSpacecraftMission(any(SpacecraftMissionRequestDTO.class)))
                .thenReturn(spacecraftMissionResponse);

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spacecraftMissionRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.spacecraftId", is(1)))
                .andExpect(jsonPath("$.missionId", is(1)));

        verify(spacecraftMissionService).createSpacecraftMission(any(SpacecraftMissionRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/spacecraft-missions - невалидные данные (null spacecraftId)")
    void createSpacecraftMission_InvalidData_NullSpacecraftId() throws Exception {
        String invalidJson = """
                {
                    "spacecraftId": null,
                    "missionId": 1
                }
                """;

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(spacecraftMissionService, never()).createSpacecraftMission(any());
    }

    @Test
    @DisplayName("POST /api/spacecraft-missions - невалидные данные (null missionId)")
    void createSpacecraftMission_InvalidData_NullMissionId() throws Exception {
        String invalidJson = """
                {
                    "spacecraftId": 1,
                    "missionId": null
                }
                """;

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(spacecraftMissionService, never()).createSpacecraftMission(any());
    }

    @Test
    @DisplayName("POST /api/spacecraft-missions - миссия не найдена")
    void createSpacecraftMission_MissionNotFound() throws Exception {
        when(spacecraftMissionService.createSpacecraftMission(any(SpacecraftMissionRequestDTO.class)))
                .thenThrow(new MissionNotFoundException("Mission not found with id: 999"));

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spacecraftMissionRequest)))
                .andExpect(status().isNotFound());

        verify(spacecraftMissionService).createSpacecraftMission(any(SpacecraftMissionRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/spacecraft-missions - комбинация уже существует")
    void createSpacecraftMission_AlreadyExists() throws Exception {
        when(spacecraftMissionService.createSpacecraftMission(any(SpacecraftMissionRequestDTO.class)))
                .thenThrow(new MissionSpacecraftExistsException("Such combination already exists!"));

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spacecraftMissionRequest)))
                .andExpect(status().isConflict());

        verify(spacecraftMissionService).createSpacecraftMission(any(SpacecraftMissionRequestDTO.class));
    }
}

