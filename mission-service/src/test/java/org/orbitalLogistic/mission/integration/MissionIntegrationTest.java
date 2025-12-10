package org.orbitalLogistic.mission.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.mission.TestcontainersConfiguration;
import org.orbitalLogistic.mission.clients.SpacecraftServiceClient;
import org.orbitalLogistic.mission.clients.UserServiceClient;
import org.orbitalLogistic.mission.dto.request.MissionRequestDTO;
import org.orbitalLogistic.mission.entities.enums.MissionPriority;
import org.orbitalLogistic.mission.entities.enums.MissionType;
import org.orbitalLogistic.mission.repositories.MissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class MissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MissionRepository missionRepository;

    @MockitoBean
    private UserServiceClient userServiceClient;

    @MockitoBean
    private SpacecraftServiceClient spacecraftServiceClient;

    @BeforeEach
    void setUp() {
        missionRepository.deleteAll();

        when(userServiceClient.userExists(anyLong())).thenReturn(true);
        when(spacecraftServiceClient.spacecraftExists(anyLong())).thenReturn(true);
    }

    @Test
    @DisplayName("Интеграционный тест: создание, получение и удаление миссии")
    void missionLifecycle_Integration() throws Exception {
        MissionRequestDTO request = new MissionRequestDTO(
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(1),
                1L,
                1L,
                MissionPriority.HIGH,
                MissionType.CARGO_TRANSPORT,
                "Integration Test Mission",
                "INT-001"
        );

        String createdResponse = mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.missionCode", is("INT-001")))
                .andExpect(jsonPath("$.missionName", is("Integration Test Mission")))
                .andExpect(jsonPath("$.priority", is("HIGH")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long missionId = objectMapper.readTree(createdResponse).get("id").asLong();

        mockMvc.perform(get("/api/missions/" + missionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(missionId.intValue())))
                .andExpect(jsonPath("$.missionCode", is("INT-001")))
                .andExpect(jsonPath("$.missionName", is("Integration Test Mission")));

        mockMvc.perform(get("/api/missions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].missionCode", is("INT-001")));

        mockMvc.perform(delete("/api/missions/" + missionId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/missions/" + missionId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Интеграционный тест: обновление миссии")
    void updateMission_Integration() throws Exception {
        MissionRequestDTO createRequest = new MissionRequestDTO(
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(1),
                1L,
                1L,
                MissionPriority.MEDIUM,
                MissionType.PERSONNEL_TRANSPORT,
                "Original Mission",
                "UPD-001"
        );

        String createdResponse = mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long missionId = objectMapper.readTree(createdResponse).get("id").asLong();

        MissionRequestDTO updateRequest = new MissionRequestDTO(
                LocalDateTime.now().plusDays(12),
                LocalDateTime.now().plusDays(2),
                1L,
                1L,
                MissionPriority.CRITICAL,
                MissionType.SCIENCE_EXPEDITION,
                "Updated Mission",
                "UPD-001"
        );

        mockMvc.perform(put("/api/missions/" + missionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missionName", is("Updated Mission")))
                .andExpect(jsonPath("$.priority", is("CRITICAL")))
                .andExpect(jsonPath("$.missionType", is("SCIENCE_EXPEDITION")));
    }

    @Test
    @DisplayName("Интеграционный тест: поиск миссий с фильтрами")
    void searchMissions_Integration() throws Exception {
        MissionRequestDTO mission1 = new MissionRequestDTO(
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(1),
                1L,
                1L,
                MissionPriority.HIGH,
                MissionType.CARGO_TRANSPORT,
                "Cargo Mission Alpha",
                "SEARCH-001"
        );

        MissionRequestDTO mission2 = new MissionRequestDTO(
                LocalDateTime.now().plusDays(15),
                LocalDateTime.now().plusDays(3),
                1L,
                1L,
                MissionPriority.LOW,
                MissionType.PERSONNEL_TRANSPORT,
                "Personnel Mission Beta",
                "SEARCH-002"
        );

        mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mission1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mission2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/missions/search")
                        .param("missionCode", "SEARCH")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        mockMvc.perform(get("/api/missions/search")
                        .param("missionType", "CARGO_TRANSPORT")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].missionCode", is("SEARCH-001")));
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - дублирование кода миссии")
    void createMission_DuplicateCode_ReturnsBadRequest() throws Exception {
        MissionRequestDTO request = new MissionRequestDTO(
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(1),
                1L,
                1L,
                MissionPriority.MEDIUM,
                MissionType.CARGO_TRANSPORT,
                "First Mission",
                "DUP-001"
        );

        mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        MissionRequestDTO duplicateRequest = new MissionRequestDTO(
                LocalDateTime.now().plusDays(12),
                LocalDateTime.now().plusDays(2),
                1L,
                1L,
                MissionPriority.HIGH,
                MissionType.PERSONNEL_TRANSPORT,
                "Duplicate Mission",
                "DUP-001"
        );

        mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - невалидные данные")
    void createMission_InvalidData_ReturnsBadRequest() throws Exception {
        String invalidJson = """
                {
                    "missionCode": "",
                    "missionName": "Invalid Mission",
                    "missionType": "CARGO_TRANSPORT",
                    "priority": "HIGH",
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
    }
}

