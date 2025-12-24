package org.orbitalLogistic.spacecraft.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.spacecraft.TestcontainersConfiguration;
import org.orbitalLogistic.spacecraft.config.TestSecurityConfig;
import org.orbitalLogistic.spacecraft.dto.request.SpacecraftRequestDTO;
import org.orbitalLogistic.spacecraft.entities.Spacecraft;
import org.orbitalLogistic.spacecraft.entities.SpacecraftType;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftClassification;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.spacecraft.repositories.SpacecraftRepository;
import org.orbitalLogistic.spacecraft.repositories.SpacecraftTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import({TestcontainersConfiguration.class, TestSecurityConfig.class})
@ActiveProfiles("test")
@Tag("integration-tests")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class SpacecraftIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

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

        var created = webTestClient.post().uri("/api/spacecrafts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        Long spacecraftId = objectMapper.readTree(new String(created.getResponseBody())).get("id").asLong();

        webTestClient.get().uri("/api/spacecrafts/" + spacecraftId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(spacecraftId.intValue())
                .jsonPath("$.registryCode").isEqualTo("SC-INT-001")
                .jsonPath("$.name").isEqualTo("Integration Test Ship");

        webTestClient.get().uri("/api/spacecrafts/" + spacecraftId + "/exists")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("true");

        SpacecraftRequestDTO updateRequest = new SpacecraftRequestDTO(
                "SC-INT-001",
                "Updated Integration Ship",
                testSpacecraftType.getId(),
                new BigDecimal("12000.00"),
                new BigDecimal("6000.00"),
                SpacecraftStatus.DOCKED,
                "Updated Location"
        );

        webTestClient.put().uri("/api/spacecrafts/" + spacecraftId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Updated Integration Ship")
                .jsonPath("$.currentLocation").isEqualTo("Updated Location");

        webTestClient.put().uri(uriBuilder -> uriBuilder.path("/api/spacecrafts/" + spacecraftId + "/status").queryParam("status", "IN_TRANSIT").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("IN_TRANSIT");

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/api/spacecrafts").queryParam("name", "Updated Integration").queryParam("page", "0").queryParam("size", "20").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].id").isEqualTo(spacecraftId.intValue())
                .jsonPath("$.totalElements").isEqualTo(1);

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

        webTestClient.post().uri("/api/spacecrafts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(duplicateRequest)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("Получение несуществующего корабля возвращает 404")
    void getSpacecraftById_NotFound_Returns404() throws Exception {
        webTestClient.get().uri("/api/spacecrafts/999999")
                .exchange()
                .expectStatus().isNotFound();
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

        webTestClient.put().uri("/api/spacecrafts/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound();
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

        webTestClient.post().uri("/api/spacecrafts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
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

        webTestClient.post().uri("/api/spacecrafts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
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

        webTestClient.get().uri("/api/spacecrafts/available")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].status").value(org.hamcrest.Matchers.equalTo("DOCKED"))
                .jsonPath("$[1].status").value(org.hamcrest.Matchers.equalTo("DOCKED"));
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

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/api/spacecrafts").queryParam("name", "Star").queryParam("page", "0").queryParam("size", "20").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].name").value(org.hamcrest.Matchers.equalTo("Star Carrier Alpha"));
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

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/api/spacecrafts").queryParam("status", "IN_TRANSIT").queryParam("page", "0").queryParam("size", "20").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].status").value(org.hamcrest.Matchers.equalTo("IN_TRANSIT"));
    }
}
