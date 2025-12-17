package org.orbitalLogistic.spacecraft.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.spacecraft.TestcontainersConfiguration;
import org.orbitalLogistic.spacecraft.dto.request.SpacecraftRequestDTO;
import org.orbitalLogistic.spacecraft.entities.Spacecraft;
import org.orbitalLogistic.spacecraft.entities.SpacecraftType;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftClassification;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.spacecraft.repositories.SpacecraftRepository;
import org.orbitalLogistic.spacecraft.repositories.SpacecraftTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Tag("integration-tests")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class SpacecraftIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpacecraftRepository spacecraftRepository;

    @Autowired
    private SpacecraftTypeRepository spacecraftTypeRepository;

    private SpacecraftType testSpacecraftType;

    @BeforeEach
    void setUp() {
        spacecraftRepository.deleteAll();
        spacecraftTypeRepository.deleteAll();

        testSpacecraftType = SpacecraftType.builder()
                .typeName("Cargo Hauler")
                .classification(SpacecraftClassification.CARGO_HAULER)
                .maxCrewCapacity(10)
                .build();
        testSpacecraftType = spacecraftTypeRepository.save(testSpacecraftType);
    }

    @Test
    @DisplayName("Интеграционный тест: полный жизненный цикл корабля")
    void fullSpacecraftLifecycle() throws Exception {
        SpacecraftRequestDTO createRequest = new SpacecraftRequestDTO(
                "SC-INT-001",
                "Integration Test Ship",
                testSpacecraftType.getId(),
                new BigDecimal("10000.00"),
                new BigDecimal("5000.00"),
                SpacecraftStatus.DOCKED,
                "Test Location"
        );

        String createResponse = mockMvc.perform(post("/api/spacecrafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registryCode").value("SC-INT-001"))
                .andExpect(jsonPath("$.name").value("Integration Test Ship"))
                .andExpect(jsonPath("$.status").value("DOCKED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long spacecraftId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/spacecrafts/" + spacecraftId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(spacecraftId))
                .andExpect(jsonPath("$.registryCode").value("SC-INT-001"))
                .andExpect(jsonPath("$.name").value("Integration Test Ship"));

        mockMvc.perform(get("/api/spacecrafts/" + spacecraftId + "/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        SpacecraftRequestDTO updateRequest = new SpacecraftRequestDTO(
                "SC-INT-001",
                "Updated Integration Ship",
                testSpacecraftType.getId(),
                new BigDecimal("12000.00"),
                new BigDecimal("6000.00"),
                SpacecraftStatus.DOCKED,
                "Updated Location"
        );

        mockMvc.perform(put("/api/spacecrafts/" + spacecraftId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Integration Ship"))
                .andExpect(jsonPath("$.currentLocation").value("Updated Location"));

        mockMvc.perform(put("/api/spacecrafts/" + spacecraftId + "/status")
                        .param("status", "IN_TRANSIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));

        mockMvc.perform(get("/api/spacecrafts")
                        .param("name", "Updated Integration")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(spacecraftId))
                .andExpect(jsonPath("$.totalElements").value(1));

        assertThat(spacecraftRepository.findById(spacecraftId)).isPresent();
    }

    @Test
    @DisplayName("Создание корабля с дублирующимся регистрационным кодом возвращает ошибку")
    void createSpacecraft_DuplicateRegistryCode_ReturnsError() throws Exception {
        Spacecraft existingSpacecraft = Spacecraft.builder()
                .registryCode("SC-DUPLICATE")
                .name("Existing Ship")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("5000.00"))
                .volumeCapacity(new BigDecimal("2500.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Earth")
                .build();
        spacecraftRepository.save(existingSpacecraft);

        SpacecraftRequestDTO duplicateRequest = new SpacecraftRequestDTO(
                "SC-DUPLICATE",
                "New Ship",
                testSpacecraftType.getId(),
                new BigDecimal("6000.00"),
                new BigDecimal("3000.00"),
                SpacecraftStatus.DOCKED,
                "Mars"
        );

        mockMvc.perform(post("/api/spacecrafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Получение несуществующего корабля возвращает 404")
    void getSpacecraftById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/spacecrafts/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Обновление несуществующего корабля возвращает 404")
    void updateSpacecraft_NotFound_Returns404() throws Exception {
        SpacecraftRequestDTO updateRequest = new SpacecraftRequestDTO(
                "SC-NOTFOUND",
                "Not Found Ship",
                testSpacecraftType.getId(),
                new BigDecimal("5000.00"),
                new BigDecimal("2500.00"),
                SpacecraftStatus.DOCKED,
                "Nowhere"
        );

        mockMvc.perform(put("/api/spacecrafts/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Валидация: пустой регистрационный код")
    void createSpacecraft_EmptyRegistryCode_ReturnsValidationError() throws Exception {
        SpacecraftRequestDTO invalidRequest = new SpacecraftRequestDTO(
                "", // пустой код
                "Valid Name",
                testSpacecraftType.getId(),
                new BigDecimal("5000.00"),
                new BigDecimal("2500.00"),
                SpacecraftStatus.DOCKED,
                "Earth"
        );

        mockMvc.perform(post("/api/spacecrafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Валидация: отрицательная емкость")
    void createSpacecraft_NegativeCapacity_ReturnsValidationError() throws Exception {
        SpacecraftRequestDTO invalidRequest = new SpacecraftRequestDTO(
                "SC-INVALID",
                "Invalid Ship",
                testSpacecraftType.getId(),
                new BigDecimal("-1000.00"), // отрицательная масса
                new BigDecimal("2500.00"),
                SpacecraftStatus.DOCKED,
                "Earth"
        );

        mockMvc.perform(post("/api/spacecrafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение доступных кораблей")
    void getAvailableSpacecrafts() throws Exception {
        Spacecraft available1 = Spacecraft.builder()
                .registryCode("SC-AVAIL-1")
                .name("Available Ship 1")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("5000.00"))
                .volumeCapacity(new BigDecimal("2500.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Earth")
                .build();

        Spacecraft available2 = Spacecraft.builder()
                .registryCode("SC-AVAIL-2")
                .name("Available Ship 2")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("6000.00"))
                .volumeCapacity(new BigDecimal("3000.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Mars")
                .build();

        Spacecraft inMission = Spacecraft.builder()
                .registryCode("SC-MISSION")
                .name("Mission Ship")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("7000.00"))
                .volumeCapacity(new BigDecimal("3500.00"))
                .status(SpacecraftStatus.IN_TRANSIT)
                .currentLocation("Jupiter")
                .build();

        spacecraftRepository.save(available1);
        spacecraftRepository.save(available2);
        spacecraftRepository.save(inMission);

        mockMvc.perform(get("/api/spacecrafts/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("DOCKED"))
                .andExpect(jsonPath("$[1].status").value("DOCKED"));
    }

    @Test
    @DisplayName("Фильтрация кораблей по имени")
    void getSpacecrafts_FilterByName() throws Exception {
        Spacecraft spacecraft1 = Spacecraft.builder()
                .registryCode("SC-STAR-1")
                .name("Star Carrier Alpha")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("10000.00"))
                .volumeCapacity(new BigDecimal("5000.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Earth")
                .build();

        Spacecraft spacecraft2 = Spacecraft.builder()
                .registryCode("SC-NOVA")
                .name("Nova Transporter")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("8000.00"))
                .volumeCapacity(new BigDecimal("4000.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Mars")
                .build();

        spacecraftRepository.save(spacecraft1);
        spacecraftRepository.save(spacecraft2);

        mockMvc.perform(get("/api/spacecrafts")
                        .param("name", "Star")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Star Carrier Alpha"));
    }

    @Test
    @DisplayName("Фильтрация кораблей по статусу")
    void getSpacecrafts_FilterByStatus() throws Exception {
        Spacecraft available = Spacecraft.builder()
                .registryCode("SC-AVAIL")
                .name("Available Ship")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("5000.00"))
                .volumeCapacity(new BigDecimal("2500.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Earth")
                .build();

        Spacecraft inMission = Spacecraft.builder()
                .registryCode("SC-MISSION")
                .name("Mission Ship")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("6000.00"))
                .volumeCapacity(new BigDecimal("3000.00"))
                .status(SpacecraftStatus.IN_TRANSIT)
                .currentLocation("Mars")
                .build();

        spacecraftRepository.save(available);
        spacecraftRepository.save(inMission);

        mockMvc.perform(get("/api/spacecrafts")
                        .param("status", "IN_TRANSIT")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("IN_TRANSIT"));
    }
}

