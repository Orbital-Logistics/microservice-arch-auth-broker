package org.orbitalLogistic.mission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.mission.controllers.MissionController;
import org.orbitalLogistic.mission.dto.common.PageResponseDTO;
import org.orbitalLogistic.mission.dto.request.MissionRequestDTO;
import org.orbitalLogistic.mission.dto.response.MissionResponseDTO;
import org.orbitalLogistic.mission.entities.enums.MissionPriority;
import org.orbitalLogistic.mission.entities.enums.MissionStatus;
import org.orbitalLogistic.mission.entities.enums.MissionType;
import org.orbitalLogistic.mission.exceptions.MissionNotFoundException;
import org.orbitalLogistic.mission.services.MissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MissionController.class)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class MissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MissionService missionService;

    private MissionResponseDTO missionResponse;
    private MissionRequestDTO missionRequest;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime scheduledArrival;

    @BeforeEach
    void setUp() {
        scheduledDeparture = LocalDateTime.of(2025, 12, 15, 10, 0);
        scheduledArrival = LocalDateTime.of(2025, 12, 25, 10, 0);

        missionResponse = new MissionResponseDTO(
                1L,
                "MISS-001",
                "Test Mission",
                MissionType.CARGO_TRANSPORT,
                MissionStatus.PLANNING,
                MissionPriority.MEDIUM,
                1L,
                "TestCommander",
                1L,
                "TestSpacecraft",
                scheduledDeparture,
                scheduledArrival,
                5
        );

        missionRequest = new MissionRequestDTO(
                scheduledArrival,
                scheduledDeparture,
                1L,
                1L,
                MissionPriority.MEDIUM,
                MissionType.CARGO_TRANSPORT,
                "Test Mission",
                "MISS-001"
        );
    }

    @Test
    @DisplayName("GET /api/missions - получение всех миссий")
    void getAllMissions_Success() throws Exception {
        PageResponseDTO<MissionResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(missionResponse), 0, 20, 1, 1, true, true
        );
        when(missionService.getAllMissions(0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/missions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].missionCode", is("MISS-001")))
                .andExpect(jsonPath("$.totalElements", is(1)));

        verify(missionService).getAllMissions(0, 20);
    }

    @Test
    @DisplayName("GET /api/missions - пустой список")
    void getAllMissions_EmptyList() throws Exception {
        PageResponseDTO<MissionResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(), 0, 20, 0, 0, true, true
        );
        when(missionService.getAllMissions(0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/missions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)));

        verify(missionService).getAllMissions(0, 20);
    }

    @Test
    @DisplayName("GET /api/missions/search - поиск с фильтрами")
    void searchMissions_WithFilters() throws Exception {
        PageResponseDTO<MissionResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(missionResponse), 0, 20, 1, 1, true, true
        );
        when(missionService.searchMissions("MISS", "PLANNING", "CARGO_TRANSPORT", 0, 20))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/missions/search")
                        .param("missionCode", "MISS")
                        .param("status", "PLANNING")
                        .param("missionType", "CARGO_TRANSPORT")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].missionCode", is("MISS-001")));

        verify(missionService).searchMissions("MISS", "PLANNING", "CARGO_TRANSPORT", 0, 20);
    }

    @Test
    @DisplayName("GET /api/missions/{id} - получение миссии по ID")
    void getMissionById_Success() throws Exception {
        when(missionService.getMissionById(1L)).thenReturn(missionResponse);

        mockMvc.perform(get("/api/missions/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.missionCode", is("MISS-001")))
                .andExpect(jsonPath("$.missionName", is("Test Mission")))
                .andExpect(jsonPath("$.status", is("PLANNING")));

        verify(missionService).getMissionById(1L);
    }

    @Test
    @DisplayName("GET /api/missions/{id} - миссия не найдена")
    void getMissionById_NotFound() throws Exception {
        when(missionService.getMissionById(999L))
                .thenThrow(new MissionNotFoundException("Mission not found with id: 999"));

        mockMvc.perform(get("/api/missions/999"))
                .andExpect(status().isNotFound());

        verify(missionService).getMissionById(999L);
    }

    @Test
    @DisplayName("POST /api/missions - создание миссии")
    void createMission_Success() throws Exception {
        when(missionService.createMission(any(MissionRequestDTO.class))).thenReturn(missionResponse);

        mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missionRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.missionCode", is("MISS-001")))
                .andExpect(jsonPath("$.missionName", is("Test Mission")));

        verify(missionService).createMission(any(MissionRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/missions - невалидные данные (пустой код)")
    void createMission_InvalidData_EmptyCode() throws Exception {
        MissionRequestDTO invalidRequest = new MissionRequestDTO(
                scheduledArrival,
                scheduledDeparture,
                1L,
                1L,
                MissionPriority.MEDIUM,
                MissionType.CARGO_TRANSPORT,
                "Test Mission",
                ""
        );

        mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(missionService, never()).createMission(any());
    }

    @Test
    @DisplayName("POST /api/missions - невалидные данные (null тип миссии)")
    void createMission_InvalidData_NullMissionType() throws Exception {
        String invalidJson = """
                {
                    "missionCode": "MISS-001",
                    "missionName": "Test Mission",
                    "missionType": null,
                    "priority": "MEDIUM",
                    "commandingOfficerId": 1,
                    "spacecraftId": 1,
                    "scheduledDeparture": "2025-12-15T10:00:00",
                    "scheduledArrival": "2025-12-25T10:00:00"
                }
                """;

        mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(missionService, never()).createMission(any());
    }

    @Test
    @DisplayName("PUT /api/missions/{id} - обновление миссии")
    void updateMission_Success() throws Exception {
        when(missionService.updateMission(eq(1L), any(MissionRequestDTO.class)))
                .thenReturn(missionResponse);

        mockMvc.perform(put("/api/missions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missionRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.missionCode", is("MISS-001")));

        verify(missionService).updateMission(eq(1L), any(MissionRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/missions/{id} - миссия не найдена")
    void updateMission_NotFound() throws Exception {
        when(missionService.updateMission(eq(999L), any(MissionRequestDTO.class)))
                .thenThrow(new MissionNotFoundException("Mission not found with id: 999"));

        mockMvc.perform(put("/api/missions/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missionRequest)))
                .andExpect(status().isNotFound());

        verify(missionService).updateMission(eq(999L), any(MissionRequestDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/missions/{id}/status - обновление статуса миссии")
    void updateMissionStatus_Success() throws Exception {
        MissionResponseDTO updatedResponse = new MissionResponseDTO(
                1L, "MISS-001", "Test Mission", MissionType.CARGO_TRANSPORT,
                MissionStatus.IN_PROGRESS, MissionPriority.MEDIUM, 1L,
                "TestCommander", 1L, "TestSpacecraft",
                scheduledDeparture, scheduledArrival, 5
        );
        when(missionService.updateMissionStatus(1L, MissionStatus.IN_PROGRESS))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/missions/1/status")
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        verify(missionService).updateMissionStatus(1L, MissionStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("DELETE /api/missions/{id} - удаление миссии")
    void deleteMission_Success() throws Exception {
        doNothing().when(missionService).deleteMission(1L);

        mockMvc.perform(delete("/api/missions/1"))
                .andExpect(status().isNoContent());

        verify(missionService).deleteMission(1L);
    }

    @Test
    @DisplayName("DELETE /api/missions/{id} - миссия не найдена")
    void deleteMission_NotFound() throws Exception {
        doThrow(new MissionNotFoundException("Mission not found with id: 999"))
                .when(missionService).deleteMission(999L);

        mockMvc.perform(delete("/api/missions/999"))
                .andExpect(status().isNotFound());

        verify(missionService).deleteMission(999L);
    }

    @Test
    @DisplayName("GET /api/missions/commander/{commanderId} - получение миссий по командиру")
    void getMissionsByCommander_Success() throws Exception {
        when(missionService.getMissionsByCommander(1L))
                .thenReturn(List.of(missionResponse));

        mockMvc.perform(get("/api/missions/commander/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].commandingOfficerId", is(1)));

        verify(missionService).getMissionsByCommander(1L);
    }

    @Test
    @DisplayName("GET /api/missions/spacecraft/{spacecraftId} - получение миссий по кораблю")
    void getMissionsBySpacecraft_Success() throws Exception {
        when(missionService.getMissionsBySpacecraft(1L))
                .thenReturn(List.of(missionResponse));

        mockMvc.perform(get("/api/missions/spacecraft/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].spacecraftId", is(1)));

        verify(missionService).getMissionsBySpacecraft(1L);
    }
}

