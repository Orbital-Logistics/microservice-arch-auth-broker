package org.orbitalLogistic.spacecraft.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.spacecraft.TestcontainersConfiguration;
import org.orbitalLogistic.spacecraft.config.TestSecurityConfig;
import org.orbitalLogistic.spacecraft.dto.request.SpacecraftTypeRequestDTO;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftClassification;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import({TestcontainersConfiguration.class, TestSecurityConfig.class})
@ActiveProfiles("test")
@Tag("integration-tests")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class SpacecraftTypeIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpacecraftTypeRepository spacecraftTypeRepository;

    @Autowired
    private SpacecraftRepository spacecraftRepository;

    @BeforeEach
    void setUp() {
        spacecraftRepository.deleteAll();
        spacecraftTypeRepository.deleteAll();
    }

    @Test
    @DisplayName("Интеграционный тест: создание, получение и список типов кораблей")
    void spacecraftTypeLifecycle_Integration() throws Exception {
        SpacecraftTypeRequestDTO request = new SpacecraftTypeRequestDTO(
                "Heavy Cargo Hauler",
                SpacecraftClassification.CARGO_HAULER,
                15
        );

        var created = webTestClient.post()
                .uri("/api/spacecraft-types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        String createdResponse = new String(created.getResponseBody());

        Long typeId = objectMapper.readTree(createdResponse).get("id").asLong();

        webTestClient.get().uri("/api/spacecraft-types/" + typeId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(typeId.intValue())
                .jsonPath("$.typeName").isEqualTo("Heavy Cargo Hauler")
                .jsonPath("$.classification").isEqualTo("CARGO_HAULER");

        webTestClient.get().uri("/api/spacecraft-types")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isEqualTo(typeId.intValue());
    }

    @Test
    @DisplayName("Интеграционный тест: создание нескольких типов кораблей")
    void createMultipleSpacecraftTypes_Integration() throws Exception {
        SpacecraftTypeRequestDTO cargoHauler = new SpacecraftTypeRequestDTO(
                "Cargo Hauler",
                SpacecraftClassification.CARGO_HAULER,
                10
        );

        SpacecraftTypeRequestDTO passenger = new SpacecraftTypeRequestDTO(
                "Personnel Transport",
                SpacecraftClassification.PERSONNEL_TRANSPORT,
                50
        );

        SpacecraftTypeRequestDTO science = new SpacecraftTypeRequestDTO(
                "Science Vessel",
                SpacecraftClassification.SCIENCE_VESSEL,
                25
        );

        webTestClient.post().uri("/api/spacecraft-types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cargoHauler)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.classification").isEqualTo("CARGO_HAULER");

        webTestClient.post().uri("/api/spacecraft-types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(passenger)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.classification").isEqualTo("PERSONNEL_TRANSPORT");

        webTestClient.post().uri("/api/spacecraft-types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(science)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.classification").isEqualTo("SCIENCE_VESSEL");

        webTestClient.get().uri("/api/spacecraft-types")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3);
    }

    @Test
    @DisplayName("Интеграционный тест: получение несуществующего типа возвращает 404")
    void getSpacecraftTypeById_NotFound_Returns404() throws Exception {
        webTestClient.get().uri("/api/spacecraft-types/999999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - пустое имя типа")
    void createSpacecraftType_EmptyTypeName_ReturnsBadRequest() throws Exception {
        String invalidJson = """
                {
                    "typeName": "",
                    "classification": "CARGO_HAULER",
                    "maxCrewCapacity": 10
                }
                """;

        webTestClient.post().uri("/api/spacecraft-types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - null классификация")
    void createSpacecraftType_NullClassification_ReturnsBadRequest() throws Exception {
        String invalidJson = """
                {
                    "typeName": "Test Type",
                    "classification": null,
                    "maxCrewCapacity": 10
                }
                """;

        webTestClient.post().uri("/api/spacecraft-types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - отрицательная вместимость экипажа")
    void createSpacecraftType_NegativeMaxCrewCapacity_ReturnsBadRequest() throws Exception {
        String invalidJson = """
                {
                    "typeName": "Test Type",
                    "classification": "CARGO_HAULER",
                    "maxCrewCapacity": -5
                }
                """;

        webTestClient.post().uri("/api/spacecraft-types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Интеграционный тест: проверка всех типов классификаций")
    void createAllClassificationTypes_Integration() throws Exception {
        SpacecraftClassification[] classifications = SpacecraftClassification.values();

        for (int i = 0; i < classifications.length; i++) {
            SpacecraftTypeRequestDTO request = new SpacecraftTypeRequestDTO(
                    classifications[i].name() + " Type",
                    classifications[i],
                    10 + i
            );

            webTestClient.post().uri("/api/spacecraft-types")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.classification").isEqualTo(classifications[i].name());
        }

        webTestClient.get().uri("/api/spacecraft-types")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(classifications.length);
    }

    @Test
    @DisplayName("Интеграционный тест: граничные значения вместимости экипажа")
    void createSpacecraftType_BoundaryValues() throws Exception {
        SpacecraftTypeRequestDTO minCrew = new SpacecraftTypeRequestDTO(
                "Min Crew Type",
                SpacecraftClassification.SCIENCE_VESSEL,
                1
        );

        webTestClient.post().uri("/api/spacecraft-types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(minCrew)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.maxCrewCapacity").isEqualTo(1);

        SpacecraftTypeRequestDTO largeCrew = new SpacecraftTypeRequestDTO(
                "Large Crew Type",
                SpacecraftClassification.PERSONNEL_TRANSPORT,
                1000
        );

        webTestClient.post().uri("/api/spacecraft-types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(largeCrew)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.maxCrewCapacity").isEqualTo(1000);
    }
}
