package org.orbitalLogistic.mission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.mission.controllers.MissionAssignmentController;
import org.orbitalLogistic.mission.dto.common.PageResponseDTO;
import org.orbitalLogistic.mission.dto.request.MissionAssignmentRequestDTO;
import org.orbitalLogistic.mission.dto.response.MissionAssignmentResponseDTO;
import org.orbitalLogistic.mission.entities.enums.AssignmentRole;
import org.orbitalLogistic.mission.exceptions.MissionAssignmentNotFoundException;
import org.orbitalLogistic.mission.services.JwtService;
import org.orbitalLogistic.mission.services.MissionAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MissionAssignmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class MissionAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MissionAssignmentService missionAssignmentService;

    @MockitoBean
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockitoBean
    private JwtService jwtService;

    private MissionAssignmentResponseDTO assignmentResponse;
    private MissionAssignmentRequestDTO assignmentRequest;
    private LocalDateTime assignedAt;

    @BeforeEach
    void setUp() {
        assignedAt = LocalDateTime.of(2025, 12, 10, 10, 0);

        assignmentResponse = new MissionAssignmentResponseDTO(
                1L,
                1L,
                "Test Mission",
                1L,
                "John Doe",
                assignedAt,
                AssignmentRole.PILOT,
                "Navigation Systems"
        );

        assignmentRequest = new MissionAssignmentRequestDTO(
                1L,
                1L,
                AssignmentRole.PILOT,
                "Navigation Systems"
        );
    }

    @Test
    @DisplayName("GET /api/mission-assignments - получение всех назначений")
    void getAllAssignments_Success() throws Exception {
        PageResponseDTO<MissionAssignmentResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(assignmentResponse), 0, 20, 1, 1, true, true
        );
        when(missionAssignmentService.getAllAssignments(0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/mission-assignments")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].assignmentRole", is("PILOT")));

        verify(missionAssignmentService).getAllAssignments(0, 20);
    }

    @Test
    @DisplayName("GET /api/mission-assignments/{id} - получение назначения по ID")
    void getAssignmentById_Success() throws Exception {
        when(missionAssignmentService.getAssignmentById(1L)).thenReturn(assignmentResponse);

        mockMvc.perform(get("/api/mission-assignments/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.missionName", is("Test Mission")))
                .andExpect(jsonPath("$.userName", is("John Doe")))
                .andExpect(jsonPath("$.assignmentRole", is("PILOT")));

        verify(missionAssignmentService).getAssignmentById(1L);
    }

    @Test
    @DisplayName("GET /api/mission-assignments/{id} - назначение не найдено")
    void getAssignmentById_NotFound() throws Exception {
        when(missionAssignmentService.getAssignmentById(999L))
                .thenThrow(new MissionAssignmentNotFoundException("Assignment not found with id: 999"));

        mockMvc.perform(get("/api/mission-assignments/999"))
                .andExpect(status().isNotFound());

        verify(missionAssignmentService).getAssignmentById(999L);
    }

    @Test
    @DisplayName("GET /api/mission-assignments/mission/{missionId} - получение назначений по миссии")
    void getAssignmentsByMission_Success() throws Exception {
        when(missionAssignmentService.getAssignmentsByMission(1L))
                .thenReturn(List.of(assignmentResponse));

        mockMvc.perform(get("/api/mission-assignments/mission/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].missionId", is(1)));

        verify(missionAssignmentService).getAssignmentsByMission(1L);
    }

    @Test
    @DisplayName("GET /api/mission-assignments/user/{userId} - получение назначений по пользователю")
    void getAssignmentsByUser_Success() throws Exception {
        when(missionAssignmentService.getAssignmentsByUser(1L))
                .thenReturn(List.of(assignmentResponse));

        mockMvc.perform(get("/api/mission-assignments/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(1)));

        verify(missionAssignmentService).getAssignmentsByUser(1L);
    }

    @Test
    @DisplayName("POST /api/mission-assignments - создание назначения")
    void createAssignment_Success() throws Exception {
        when(missionAssignmentService.createAssignment(any(MissionAssignmentRequestDTO.class)))
                .thenReturn(assignmentResponse);

        mockMvc.perform(post("/api/mission-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.assignmentRole", is("PILOT")));

        verify(missionAssignmentService).createAssignment(any(MissionAssignmentRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/mission-assignments - невалидные данные (null missionId)")
    void createAssignment_InvalidData_NullMissionId() throws Exception {
        String invalidJson = """
                {
                    "missionId": null,
                    "userId": 1,
                    "assignmentRole": "PILOT",
                    "responsibilityZone": "Navigation Systems"
                }
                """;

        mockMvc.perform(post("/api/mission-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(missionAssignmentService, never()).createAssignment(any());
    }

    @Test
    @DisplayName("POST /api/mission-assignments - невалидные данные (null userId)")
    void createAssignment_InvalidData_NullUserId() throws Exception {
        String invalidJson = """
                {
                    "missionId": 1,
                    "userId": null,
                    "assignmentRole": "PILOT",
                    "responsibilityZone": "Navigation Systems"
                }
                """;

        mockMvc.perform(post("/api/mission-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(missionAssignmentService, never()).createAssignment(any());
    }

    @Test
    @DisplayName("POST /api/mission-assignments - невалидные данные (null assignmentRole)")
    void createAssignment_InvalidData_NullAssignmentRole() throws Exception {
        String invalidJson = """
                {
                    "missionId": 1,
                    "userId": 1,
                    "assignmentRole": null,
                    "responsibilityZone": "Navigation Systems"
                }
                """;

        mockMvc.perform(post("/api/mission-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(missionAssignmentService, never()).createAssignment(any());
    }

    @Test
    @DisplayName("DELETE /api/mission-assignments/{id} - удаление назначения")
    void deleteAssignment_Success() throws Exception {
        doNothing().when(missionAssignmentService).deleteAssignment(1L);

        mockMvc.perform(delete("/api/mission-assignments/1"))
                .andExpect(status().isNoContent());

        verify(missionAssignmentService).deleteAssignment(1L);
    }

    @Test
    @DisplayName("DELETE /api/mission-assignments/{id} - назначение не найдено")
    void deleteAssignment_NotFound() throws Exception {
        doThrow(new MissionAssignmentNotFoundException("Assignment not found with id: 999"))
                .when(missionAssignmentService).deleteAssignment(999L);

        mockMvc.perform(delete("/api/mission-assignments/999"))
                .andExpect(status().isNotFound());

        verify(missionAssignmentService).deleteAssignment(999L);
    }
}

