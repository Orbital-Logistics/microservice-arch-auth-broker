package org.orbitalLogistic.mission.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.mission.TestcontainersConfiguration;
import org.orbitalLogistic.mission.dto.request.SpacecraftMissionRequestDTO;
import org.orbitalLogistic.mission.entities.Mission;
import org.orbitalLogistic.mission.entities.enums.MissionPriority;
import org.orbitalLogistic.mission.entities.enums.MissionStatus;
import org.orbitalLogistic.mission.entities.enums.MissionType;
import org.orbitalLogistic.mission.repositories.MissionRepository;
import org.orbitalLogistic.mission.repositories.SpacecraftMissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
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
class SpacecraftMissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpacecraftMissionRepository spacecraftMissionRepository;

    @Autowired
    private MissionRepository missionRepository;

    private Mission testMission1;
    private Mission testMission2;

    @BeforeEach
    void setUp() {
        spacecraftMissionRepository.deleteAll();
        missionRepository.deleteAll();

        testMission1 = Mission.builder()
                .missionCode("TEST-SM-001")
                .missionName("First Spacecraft Mission")
                .missionType(MissionType.CARGO_TRANSPORT)
                .status(MissionStatus.PLANNING)
                .priority(MissionPriority.MEDIUM)
                .commandingOfficerId(1L)
                .spacecraftId(1L)
                .scheduledDeparture(LocalDateTime.now().plusDays(1))
                .scheduledArrival(LocalDateTime.now().plusDays(10))
                .build();
        testMission1 = missionRepository.save(testMission1);

        testMission2 = Mission.builder()
                .missionCode("TEST-SM-002")
                .missionName("Second Spacecraft Mission")
                .missionType(MissionType.PERSONNEL_TRANSPORT)
                .status(MissionStatus.SCHEDULED)
                .priority(MissionPriority.HIGH)
                .commandingOfficerId(2L)
                .spacecraftId(2L)
                .scheduledDeparture(LocalDateTime.now().plusDays(2))
                .scheduledArrival(LocalDateTime.now().plusDays(12))
                .build();
        testMission2 = missionRepository.save(testMission2);
    }

    @Test
    @DisplayName("Интеграционный тест: создание и получение назначения корабля на миссию")
    void spacecraftMissionLifecycle_Integration() throws Exception {
        SpacecraftMissionRequestDTO request = new SpacecraftMissionRequestDTO(
                1L, testMission1.getId()
        );

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.spacecraftId", is(1)))
                .andExpect(jsonPath("$.missionId", is(testMission1.getId().intValue())));

        mockMvc.perform(get("/api/spacecraft-missions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].spacecraftId", is(1)));
    }

    @Test
    @DisplayName("Интеграционный тест: получение назначений по кораблю")
    void getBySpacecraft_Integration() throws Exception {
        SpacecraftMissionRequestDTO assignment1 = new SpacecraftMissionRequestDTO(
                1L, testMission1.getId()
        );
        SpacecraftMissionRequestDTO assignment2 = new SpacecraftMissionRequestDTO(
                1L, testMission2.getId()
        );
        SpacecraftMissionRequestDTO assignment3 = new SpacecraftMissionRequestDTO(
                2L, testMission1.getId()
        );

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignment1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignment2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignment3)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/spacecraft-missions/spacecraft/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].spacecraftId", is(1)))
                .andExpect(jsonPath("$[1].spacecraftId", is(1)));

        mockMvc.perform(get("/api/spacecraft-missions/spacecraft/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].spacecraftId", is(2)));
    }

    @Test
    @DisplayName("Интеграционный тест: получение назначений по миссии")
    void getByMission_Integration() throws Exception {
        SpacecraftMissionRequestDTO assignment1 = new SpacecraftMissionRequestDTO(
                1L, testMission1.getId()
        );
        SpacecraftMissionRequestDTO assignment2 = new SpacecraftMissionRequestDTO(
                2L, testMission1.getId()
        );
        SpacecraftMissionRequestDTO assignment3 = new SpacecraftMissionRequestDTO(
                3L, testMission2.getId()
        );

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignment1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignment2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignment3)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/spacecraft-missions/mission/" + testMission1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].missionId", is(testMission1.getId().intValue())))
                .andExpect(jsonPath("$[1].missionId", is(testMission1.getId().intValue())));

        mockMvc.perform(get("/api/spacecraft-missions/mission/" + testMission2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].missionId", is(testMission2.getId().intValue())));
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - миссия не найдена")
    void createSpacecraftMission_MissionNotFound_ReturnsNotFound() throws Exception {
        SpacecraftMissionRequestDTO request = new SpacecraftMissionRequestDTO(
                1L, 999L
        );

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - дублирование комбинации")
    void createSpacecraftMission_DuplicateCombination_ReturnsConflict() throws Exception {
        SpacecraftMissionRequestDTO request = new SpacecraftMissionRequestDTO(
                1L, testMission1.getId()
        );

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - невалидные данные")
    void createSpacecraftMission_InvalidData_ReturnsBadRequest() throws Exception {
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
    }

    @Test
    @DisplayName("Интеграционный тест: множественные назначения кораблей")
    void multipleSpacecraftAssignments_Integration() throws Exception {
        for (int spacecraftId = 1; spacecraftId <= 5; spacecraftId++) {
            SpacecraftMissionRequestDTO request = new SpacecraftMissionRequestDTO(
                    (long) spacecraftId, testMission1.getId()
            );

            mockMvc.perform(post("/api/spacecraft-missions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/spacecraft-missions/mission/" + testMission1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    @DisplayName("Интеграционный тест: один корабль на несколько миссий")
    void oneSpacecraftMultipleMissions_Integration() throws Exception {
        Mission mission3 = Mission.builder()
                .missionCode("TEST-SM-003")
                .missionName("Third Mission")
                .missionType(MissionType.SCIENCE_EXPEDITION)
                .status(MissionStatus.PLANNING)
                .priority(MissionPriority.LOW)
                .commandingOfficerId(3L)
                .spacecraftId(3L)
                .scheduledDeparture(LocalDateTime.now().plusDays(3))
                .scheduledArrival(LocalDateTime.now().plusDays(13))
                .build();
        mission3 = missionRepository.save(mission3);

        SpacecraftMissionRequestDTO assignment1 = new SpacecraftMissionRequestDTO(
                1L, testMission1.getId()
        );
        SpacecraftMissionRequestDTO assignment2 = new SpacecraftMissionRequestDTO(
                1L, testMission2.getId()
        );
        SpacecraftMissionRequestDTO assignment3 = new SpacecraftMissionRequestDTO(
                1L, mission3.getId()
        );

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignment1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignment2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/spacecraft-missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignment3)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/spacecraft-missions/spacecraft/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }
}

