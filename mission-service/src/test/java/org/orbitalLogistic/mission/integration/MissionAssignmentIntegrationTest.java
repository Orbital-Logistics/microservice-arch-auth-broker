package org.orbitalLogistic.mission.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.mission.TestcontainersConfiguration;
import org.orbitalLogistic.mission.clients.resilient.ResilientUserService;
import org.orbitalLogistic.mission.dto.request.MissionAssignmentRequestDTO;
import org.orbitalLogistic.mission.entities.Mission;
import org.orbitalLogistic.mission.entities.enums.AssignmentRole;
import org.orbitalLogistic.mission.entities.enums.MissionPriority;
import org.orbitalLogistic.mission.entities.enums.MissionStatus;
import org.orbitalLogistic.mission.entities.enums.MissionType;
import org.orbitalLogistic.mission.repositories.MissionAssignmentRepository;
import org.orbitalLogistic.mission.repositories.MissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Tag("integration-tests")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
@WithMockUser(roles = "ADMIN")
class MissionAssignmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MissionAssignmentRepository missionAssignmentRepository;

    @Autowired
    private MissionRepository missionRepository;

    @MockitoBean
    private ResilientUserService userServiceClient;

    private Mission testMission;

    @BeforeEach
    void setUp() {
        missionAssignmentRepository.deleteAll();
        missionRepository.deleteAll();

        when(userServiceClient.userExists(anyLong())).thenReturn(true);

        testMission = Mission.builder()
                .missionCode("TEST-MISSION-001")
                .missionName("Test Mission for Assignments")
                .missionType(MissionType.CARGO_TRANSPORT)
                .status(MissionStatus.PLANNING)
                .priority(MissionPriority.MEDIUM)
                .commandingOfficerId(1L)
                .spacecraftId(1L)
                .scheduledDeparture(LocalDateTime.now().plusDays(1))
                .scheduledArrival(LocalDateTime.now().plusDays(10))
                .build();
        testMission = missionRepository.save(testMission);
    }

    @Test
    @DisplayName("Интеграционный тест: создание, получение и удаление назначения")
    void missionAssignmentLifecycle_Integration() throws Exception {
        MissionAssignmentRequestDTO request = new MissionAssignmentRequestDTO(
                testMission.getId(),
                1L,
                AssignmentRole.PILOT,
                "Navigation Systems"
        );

        String createdResponse = mockMvc.perform(post("/api/mission-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.missionId", is(testMission.getId().intValue())))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.assignmentRole", is("PILOT")))
                .andExpect(jsonPath("$.responsibilityZone", is("Navigation Systems")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long assignmentId = objectMapper.readTree(createdResponse).get("id").asLong();

        mockMvc.perform(get("/api/mission-assignments/" + assignmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) assignmentId)))
                .andExpect(jsonPath("$.assignmentRole", is("PILOT")));

        mockMvc.perform(get("/api/mission-assignments")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is((int) assignmentId)));

        mockMvc.perform(delete("/api/mission-assignments/" + assignmentId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/mission-assignments/" + assignmentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Интеграционный тест: получение назначений по миссии")
    void getAssignmentsByMission_Integration() throws Exception {
        MissionAssignmentRequestDTO pilot = new MissionAssignmentRequestDTO(
                testMission.getId(), 1L, AssignmentRole.PILOT, "Navigation"
        );
        MissionAssignmentRequestDTO engineer = new MissionAssignmentRequestDTO(
                testMission.getId(), 2L, AssignmentRole.ENGINEER, "Propulsion"
        );
        MissionAssignmentRequestDTO scientist = new MissionAssignmentRequestDTO(
                testMission.getId(), 3L, AssignmentRole.SCIENTIST, "Research"
        );

        mockMvc.perform(post("/api/mission-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pilot)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/mission-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(engineer)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/mission-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scientist)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/mission-assignments/mission/" + testMission.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].missionId", is(testMission.getId().intValue())))
                .andExpect(jsonPath("$[1].missionId", is(testMission.getId().intValue())))
                .andExpect(jsonPath("$[2].missionId", is(testMission.getId().intValue())));
    }

    @Test
    @DisplayName("Интеграционный тест: получение назначений по пользователю")
    void getAssignmentsByUser_Integration() throws Exception {
        Mission mission2 = Mission.builder()
                .missionCode("TEST-MISSION-002")
                .missionName("Second Test Mission")
                .missionType(MissionType.PERSONNEL_TRANSPORT)
                .status(MissionStatus.SCHEDULED)
                .priority(MissionPriority.HIGH)
                .commandingOfficerId(2L)
                .spacecraftId(2L)
                .scheduledDeparture(LocalDateTime.now().plusDays(2))
                .scheduledArrival(LocalDateTime.now().plusDays(12))
                .build();
        mission2 = missionRepository.save(mission2);

        MissionAssignmentRequestDTO assignment1 = new MissionAssignmentRequestDTO(
                testMission.getId(), 1L, AssignmentRole.PILOT, "Navigation"
        );
        MissionAssignmentRequestDTO assignment2 = new MissionAssignmentRequestDTO(
                mission2.getId(), 1L, AssignmentRole.ENGINEER, "Systems"
        );

        mockMvc.perform(post("/api/mission-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignment1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/mission-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignment2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/mission-assignments/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId", is(1)))
                .andExpect(jsonPath("$[1].userId", is(1)));
    }

    @Test
    @DisplayName("Интеграционный тест: все роли назначений")
    void createAllAssignmentRoles_Integration() throws Exception {
        AssignmentRole[] roles = AssignmentRole.values();

        for (int i = 0; i < roles.length; i++) {
            MissionAssignmentRequestDTO request = new MissionAssignmentRequestDTO(
                    testMission.getId(),
                    (long) (i + 1),
                    roles[i],
                    "Responsibility Zone " + i
            );

            mockMvc.perform(post("/api/mission-assignments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.assignmentRole", is(roles[i].name())));
        }

        mockMvc.perform(get("/api/mission-assignments/mission/" + testMission.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(roles.length)));
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - миссия не найдена")
    void createAssignment_MissionNotFound_ReturnsNotFound() throws Exception {
        MissionAssignmentRequestDTO request = new MissionAssignmentRequestDTO(
                999L, 1L, AssignmentRole.PILOT, "Navigation"
        );

        mockMvc.perform(post("/api/mission-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - пользователь не найден")
    void createAssignment_UserNotFound_ReturnsBadRequest() throws Exception {
        when(userServiceClient.userExists(999L)).thenReturn(false);

        MissionAssignmentRequestDTO request = new MissionAssignmentRequestDTO(
                testMission.getId(), 999L, AssignmentRole.PILOT, "Navigation"
        );

        mockMvc.perform(post("/api/mission-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - пользователь уже назначен")
    void createAssignment_UserAlreadyAssigned_ReturnsConflict() throws Exception {
        MissionAssignmentRequestDTO request = new MissionAssignmentRequestDTO(
                testMission.getId(), 1L, AssignmentRole.PILOT, "Navigation"
        );

        mockMvc.perform(post("/api/mission-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/mission-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - невалидные данные")
    void createAssignment_InvalidData_ReturnsBadRequest() throws Exception {
        String invalidJson = """
                {
                    "missionId": null,
                    "userId": 1,
                    "assignmentRole": "PILOT",
                    "responsibilityZone": "Navigation"
                }
                """;

        mockMvc.perform(post("/api/mission-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Интеграционный тест: пагинация работает корректно")
    void pagination_WorksCorrectly() throws Exception {
        for (int i = 1; i <= 5; i++) {
            MissionAssignmentRequestDTO request = new MissionAssignmentRequestDTO(
                    testMission.getId(),
                    (long) i,
                    AssignmentRole.values()[i % AssignmentRole.values().length],
                    "Zone " + i
            );

            mockMvc.perform(post("/api/mission-assignments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/mission-assignments")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.totalPages", is(3)));

        mockMvc.perform(get("/api/mission-assignments")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        mockMvc.perform(get("/api/mission-assignments")
                        .param("page", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }
}

