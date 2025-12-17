package org.orbitalLogistic.spacecraft.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.spacecraft.TestcontainersConfiguration;
import org.orbitalLogistic.spacecraft.dto.request.SpacecraftTypeRequestDTO;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftClassification;
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
class SpacecraftTypeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

        String createdResponse = mockMvc.perform(post("/api/spacecraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeName").value("Heavy Cargo Hauler"))
                .andExpect(jsonPath("$.classification").value("CARGO_HAULER"))
                .andExpect(jsonPath("$.maxCrewCapacity").value(15))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long typeId = objectMapper.readTree(createdResponse).get("id").asLong();

        mockMvc.perform(get("/api/spacecraft-types/" + typeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(typeId.intValue()))
                .andExpect(jsonPath("$.typeName").value("Heavy Cargo Hauler"))
                .andExpect(jsonPath("$.classification").value("CARGO_HAULER"));

        mockMvc.perform(get("/api/spacecraft-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(typeId.intValue()));
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

        mockMvc.perform(post("/api/spacecraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cargoHauler)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.classification").value("CARGO_HAULER"));

        mockMvc.perform(post("/api/spacecraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.classification").value("PERSONNEL_TRANSPORT"));

        mockMvc.perform(post("/api/spacecraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(science)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.classification").value("SCIENCE_VESSEL"));

        mockMvc.perform(get("/api/spacecraft-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("Интеграционный тест: получение несуществующего типа возвращает 404")
    void getSpacecraftTypeById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/spacecraft-types/999999"))
                .andExpect(status().isNotFound());
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

        mockMvc.perform(post("/api/spacecraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
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

        mockMvc.perform(post("/api/spacecraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
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

        mockMvc.perform(post("/api/spacecraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
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

            mockMvc.perform(post("/api/spacecraft-types")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.classification").value(classifications[i].name()));
        }

        mockMvc.perform(get("/api/spacecraft-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(classifications.length));
    }

    @Test
    @DisplayName("Интеграционный тест: граничные значения вместимости экипажа")
    void createSpacecraftType_BoundaryValues() throws Exception {
        SpacecraftTypeRequestDTO minCrew = new SpacecraftTypeRequestDTO(
                "Min Crew Type",
                SpacecraftClassification.SCIENCE_VESSEL,
                1
        );

        mockMvc.perform(post("/api/spacecraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minCrew)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maxCrewCapacity").value(1));

        SpacecraftTypeRequestDTO largeCrew = new SpacecraftTypeRequestDTO(
                "Large Crew Type",
                SpacecraftClassification.PERSONNEL_TRANSPORT,
                1000
        );

        mockMvc.perform(post("/api/spacecraft-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(largeCrew)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maxCrewCapacity").value(1000));
    }
}

