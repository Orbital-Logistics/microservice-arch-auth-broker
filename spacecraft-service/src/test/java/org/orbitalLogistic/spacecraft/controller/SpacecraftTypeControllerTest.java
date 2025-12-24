package org.orbitalLogistic.spacecraft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.spacecraft.config.TestSecurityConfig;
import org.orbitalLogistic.spacecraft.controllers.SpacecraftTypeController;
import org.orbitalLogistic.spacecraft.dto.request.SpacecraftTypeRequestDTO;
import org.orbitalLogistic.spacecraft.dto.response.SpacecraftTypeResponseDTO;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftClassification;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftTypeNotFoundException;
import org.orbitalLogistic.spacecraft.services.SpacecraftTypeService;
import org.orbitalLogistic.spacecraft.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(value = SpacecraftTypeController.class)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
@Import(TestSecurityConfig.class)
class SpacecraftTypeControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SpacecraftTypeService spacecraftTypeService;

    @MockitoBean
    private JwtService jwtService;

    private SpacecraftTypeResponseDTO spacecraftTypeResponse;
    private SpacecraftTypeRequestDTO spacecraftTypeRequest;

    @BeforeEach
    void setUp() {
        spacecraftTypeResponse = new SpacecraftTypeResponseDTO(
                1L,
                "Cargo Hauler",
                SpacecraftClassification.CARGO_HAULER,
                10
        );

        spacecraftTypeRequest = new SpacecraftTypeRequestDTO(
                "Personnel Transport",
                SpacecraftClassification.PERSONNEL_TRANSPORT,
                50
        );
    }

    @Test
    @DisplayName("GET /api/spacecraft-types - получение всех типов кораблей")
    void getAllSpacecraftTypes_Success() {
        List<SpacecraftTypeResponseDTO> types = List.of(spacecraftTypeResponse);
        when(spacecraftTypeService.getAllSpacecraftTypes()).thenReturn(Mono.just(types));

        webTestClient.get().uri("/api/spacecraft-types")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].typeName").isEqualTo("Cargo Hauler")
                .jsonPath("$[0].classification").isEqualTo("CARGO_HAULER")
                .jsonPath("$[0].maxCrewCapacity").isEqualTo(10);

        verify(spacecraftTypeService).getAllSpacecraftTypes();
    }

    @Test
    @DisplayName("GET /api/spacecraft-types - пустой список")
    void getAllSpacecraftTypes_EmptyList() {
        when(spacecraftTypeService.getAllSpacecraftTypes()).thenReturn(Mono.just(List.of()));

        webTestClient.get().uri("/api/spacecraft-types")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().jsonPath("$").isArray();

        verify(spacecraftTypeService).getAllSpacecraftTypes();
    }

    @Test
    @DisplayName("GET /api/spacecraft-types/{id} - получение типа корабля по ID")
    void getSpacecraftTypeById_Success() {
        when(spacecraftTypeService.getSpacecraftTypeById(1L)).thenReturn(Mono.just(spacecraftTypeResponse));

        webTestClient.get().uri("/api/spacecraft-types/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.typeName").isEqualTo("Cargo Hauler")
                .jsonPath("$.classification").isEqualTo("CARGO_HAULER")
                .jsonPath("$.maxCrewCapacity").isEqualTo(10);

        verify(spacecraftTypeService).getSpacecraftTypeById(1L);
    }

    @Test
    @DisplayName("GET /api/spacecraft-types/{id} - тип корабля не найден")
    void getSpacecraftTypeById_NotFound() {
        when(spacecraftTypeService.getSpacecraftTypeById(999L))
                .thenReturn(Mono.error(new SpacecraftTypeNotFoundException("Spacecraft type not found with id: 999")));

        webTestClient.get().uri("/api/spacecraft-types/999")
                .exchange()
                .expectStatus().isNotFound();

        verify(spacecraftTypeService).getSpacecraftTypeById(999L);
    }

    @Test
    @DisplayName("POST /api/spacecraft-types - создание типа корабля")
    void createSpacecraftType_Success() throws Exception {
        SpacecraftTypeResponseDTO newResponse = new SpacecraftTypeResponseDTO(
                2L,
                "Personnel Transport",
                SpacecraftClassification.PERSONNEL_TRANSPORT,
                50
        );

        when(spacecraftTypeService.createSpacecraftType(any(SpacecraftTypeRequestDTO.class)))
                .thenReturn(Mono.just(newResponse));

        webTestClient.post().uri("/api/spacecraft-types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(spacecraftTypeRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(2)
                .jsonPath("$.typeName").isEqualTo("Personnel Transport")
                .jsonPath("$.classification").isEqualTo("PERSONNEL_TRANSPORT")
                .jsonPath("$.maxCrewCapacity").isEqualTo(50);

        verify(spacecraftTypeService).createSpacecraftType(any(SpacecraftTypeRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/spacecraft-types - невалидные данные (пустое имя типа)")
    void createSpacecraftType_InvalidData_EmptyTypeName() throws Exception {
        SpacecraftTypeRequestDTO invalidRequest = new SpacecraftTypeRequestDTO(
                "",
                SpacecraftClassification.PERSONNEL_TRANSPORT,
                50
        );

        webTestClient.post().uri("/api/spacecraft-types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();

        verify(spacecraftTypeService, never()).createSpacecraftType(any());
    }

    @Test
    @DisplayName("POST /api/spacecraft-types - невалидные данные (null classification)")
    void createSpacecraftType_InvalidData_NullClassification() throws Exception {
        String invalidJson = """
                {
                    "typeName": "Test Type",
                    "classification": null,
                    "maxCrewCapacity": 50
                }
                """;

        webTestClient.post().uri("/api/spacecraft-types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().isBadRequest();

        verify(spacecraftTypeService, never()).createSpacecraftType(any());
    }

    @Test
    @DisplayName("POST /api/spacecraft-types - невалидные данные (отрицательная вместимость)")
    void createSpacecraftType_InvalidData_NegativeCapacity() throws Exception {
        SpacecraftTypeRequestDTO invalidRequest = new SpacecraftTypeRequestDTO(
                "Test Type",
                SpacecraftClassification.PERSONNEL_TRANSPORT,
                -10
        );

        webTestClient.post().uri("/api/spacecraft-types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();

        verify(spacecraftTypeService, never()).createSpacecraftType(any());
    }
}
