package org.orbitalLogistic.mission.infrastructure.adapters.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.mission.application.ports.in.*;
import org.orbitalLogistic.mission.domain.model.Mission;
import org.orbitalLogistic.mission.domain.model.enums.MissionPriority;
import org.orbitalLogistic.mission.domain.model.enums.MissionStatus;
import org.orbitalLogistic.mission.domain.model.enums.MissionType;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.request.MissionRequestDTO;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.mapper.MissionDTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MissionRestController.class)
@TestPropertySource(properties = {"spring.cloud.config.enabled=false"})
class MissionRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetMissionsUseCase getMissionsUseCase;

    @MockitoBean
    private GetMissionByIdUseCase getMissionByIdUseCase;

    @MockitoBean
    private CreateMissionUseCase createMissionUseCase;

    @MockitoBean
    private UpdateMissionUseCase updateMissionUseCase;

    @MockitoBean
    private DeleteMissionUseCase deleteMissionUseCase;

    @MockitoBean
    private SearchMissionsUseCase searchMissionsUseCase;

    @MockitoBean
    private MissionDTOMapper missionMapper;

    @MockitoBean
    private org.orbitalLogistic.mission.jwt.JwtService jwtService;

    private Mission mission;

    @BeforeEach
    void setUp() {
        mission = Mission.builder()
                .id(1L)
                .missionCode("MARS-001")
                .missionName("Mars Exploration")
                .missionType(MissionType.CARGO_TRANSPORT)
                .status(MissionStatus.PLANNING)
                .priority(MissionPriority.HIGH)
                .commandingOfficerId(1L)
                .spacecraftId(1L)
                .scheduledDeparture(LocalDateTime.now().plusDays(30))
                .scheduledArrival(LocalDateTime.now().plusDays(180))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMissionById_Success() throws Exception {
        // Given
        when(getMissionByIdUseCase.getMissionById(1L)).thenReturn(Optional.of(mission));

        // When & Then
        mockMvc.perform(get("/api/missions/{id}", 1L))
                .andExpect(status().isOk());

        verify(getMissionByIdUseCase).getMissionById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMissionById_NotFound() throws Exception {
        // Given
        when(getMissionByIdUseCase.getMissionById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/missions/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(getMissionByIdUseCase).getMissionById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMission_Success() throws Exception {
        // Given
        MissionRequestDTO requestDTO = new MissionRequestDTO(
                LocalDateTime.now().plusDays(180),
                LocalDateTime.now().plusDays(30),
                1L,
                1L,
                MissionPriority.HIGH,
                MissionType.CARGO_TRANSPORT,
                "Mars Exploration",
                "MARS-001"
        );

        when(createMissionUseCase.createMission(any(CreateMissionCommand.class))).thenReturn(mission);

        // When & Then
        mockMvc.perform(post("/api/missions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());

        verify(createMissionUseCase).createMission(any(CreateMissionCommand.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchMissions_Success() throws Exception {
        // Given
        when(searchMissionsUseCase.searchMissions(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(mission));
        when(searchMissionsUseCase.countMissions(any(), any(), any())).thenReturn(1L);

        // When & Then
        mockMvc.perform(get("/api/missions/search")
                        .param("missionCode", "MARS-001"))
                .andExpect(status().isOk());

        verify(searchMissionsUseCase).searchMissions(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMission_Success() throws Exception {
        // Given
        doNothing().when(deleteMissionUseCase).deleteMission(1L);

        // When & Then
        mockMvc.perform(delete("/api/missions/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(deleteMissionUseCase).deleteMission(1L);
    }

    @Test
    void getMissionById_Unauthorized_WhenNoAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/missions/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }
}
