package org.orbitalLogistic.spacecraft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.spacecraft.config.TestSecurityConfig;
import org.orbitalLogistic.spacecraft.controllers.SpacecraftController;
import org.orbitalLogistic.spacecraft.dto.common.PageResponseDTO;
import org.orbitalLogistic.spacecraft.dto.request.SpacecraftRequestDTO;
import org.orbitalLogistic.spacecraft.dto.response.SpacecraftResponseDTO;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftClassification;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.spacecraft.services.JwtService;
import org.orbitalLogistic.spacecraft.services.SpacecraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(value = SpacecraftController.class)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
@Import(TestSecurityConfig.class)
class SpacecraftControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SpacecraftService spacecraftService;

    @MockitoBean
    private JwtService jwtService;

    private SpacecraftResponseDTO spacecraftResponse;
    private SpacecraftRequestDTO spacecraftRequest;

    @BeforeEach
    void setUp() {
        spacecraftResponse = new SpacecraftResponseDTO(
                1L,
                "SC-001",
                "Star Carrier",
                "Cargo Hauler",
                SpacecraftClassification.CARGO_HAULER,
                new BigDecimal("10000.00"),
                new BigDecimal("5000.00"),
                SpacecraftStatus.DOCKED,
                "Mars Orbit",
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        spacecraftRequest = new SpacecraftRequestDTO(
                "SC-002",
                "Nova Transporter",
                1L,
                new BigDecimal("15000.00"),
                new BigDecimal("7500.00"),
                SpacecraftStatus.DOCKED,
                "Earth Orbit"
        );
    }

    @Test
    @DisplayName("GET /api/spacecrafts - успешное получение списка")
    void getAllSpacecrafts_Success() {
        PageResponseDTO<SpacecraftResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(spacecraftResponse),
                0,
                20,
                1L,
                1,
                true,
                true
        );

        when(spacecraftService.getSpacecrafts(any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(pageResponse));

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/api/spacecrafts").queryParam("page", "0").queryParam("size", "20").build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "1")
                .expectBody()
                .jsonPath("$.content[0].id").isEqualTo(1)
                .jsonPath("$.content[0].registryCode").isEqualTo("SC-001")
                .jsonPath("$.content[0].name").isEqualTo("Star Carrier")
                .jsonPath("$.totalElements").isEqualTo(1);

        verify(spacecraftService).getSpacecrafts(null, null, 0, 20);
    }

    @Test
    @DisplayName("GET /api/spacecrafts - с фильтрами")
    void getAllSpacecrafts_WithFilters() {
        PageResponseDTO<SpacecraftResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(spacecraftResponse),
                0,
                20,
                1L,
                1,
                true,
                true
        );

        when(spacecraftService.getSpacecrafts(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(Mono.just(pageResponse));

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/api/spacecrafts").queryParam("name", "Star").queryParam("status", "DOCKED").queryParam("page", "0").queryParam("size", "20").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].name").isEqualTo("Star Carrier")
                .jsonPath("$.content[0].status").isEqualTo("DOCKED");

        verify(spacecraftService).getSpacecrafts("Star", "DOCKED", 0, 20);
    }

    @Test
    @DisplayName("GET /api/spacecrafts/{id} - успешное получение по ID")
    void getSpacecraftById_Success() {
        when(spacecraftService.getSpacecraftById(1L)).thenReturn(Mono.just(spacecraftResponse));

        webTestClient.get().uri("/api/spacecrafts/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.registryCode").isEqualTo("SC-001")
                .jsonPath("$.name").isEqualTo("Star Carrier");

        verify(spacecraftService).getSpacecraftById(1L);
    }

    @Test
    @DisplayName("POST /api/spacecrafts - успешное создание")
    void createSpacecraft_Success() {
        when(spacecraftService.createSpacecraft(any(SpacecraftRequestDTO.class)))
                .thenReturn(Mono.just(spacecraftResponse));

        webTestClient.post().uri("/api/spacecrafts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(spacecraftRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.registryCode").isEqualTo("SC-001");

        verify(spacecraftService).createSpacecraft(any(SpacecraftRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/spacecrafts - невалидные данные")
    void createSpacecraft_InvalidData() {
        SpacecraftRequestDTO invalidRequest = new SpacecraftRequestDTO(
                "", // пустой registryCode
                "Nova Transporter",
                1L,
                new BigDecimal("15000.00"),
                new BigDecimal("7500.00"),
                SpacecraftStatus.DOCKED,
                "Earth Orbit"
        );

        webTestClient.post().uri("/api/spacecrafts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("PUT /api/spacecrafts/{id} - успешное обновление")
    void updateSpacecraft_Success() {
        SpacecraftResponseDTO updatedResponse = new SpacecraftResponseDTO(
                1L,
                "SC-001",
                "Updated Name",
                "Cargo Hauler",
                SpacecraftClassification.CARGO_HAULER,
                new BigDecimal("10000.00"),
                new BigDecimal("5000.00"),
                SpacecraftStatus.DOCKED,
                "Mars Orbit",
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(spacecraftService.updateSpacecraft(eq(1L), any(SpacecraftRequestDTO.class)))
                .thenReturn(Mono.just(updatedResponse));

        webTestClient.put().uri("/api/spacecrafts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(spacecraftRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Updated Name");

        verify(spacecraftService).updateSpacecraft(eq(1L), any(SpacecraftRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/spacecrafts/available - получение доступных кораблей")
    void getAvailableSpacecrafts() {
        when(spacecraftService.getAvailableSpacecrafts())
                .thenReturn(Mono.just(List.of(spacecraftResponse)));

        webTestClient.get().uri("/api/spacecrafts/available")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].status").isEqualTo("DOCKED");

        verify(spacecraftService).getAvailableSpacecrafts();
    }

    @Test
    @DisplayName("PUT /api/spacecrafts/{id}/status - обновление статуса")
    void updateSpacecraftStatus() {
        SpacecraftResponseDTO updatedResponse = new SpacecraftResponseDTO(
                1L,
                "SC-001",
                "Star Carrier",
                "Cargo Hauler",
                SpacecraftClassification.CARGO_HAULER,
                new BigDecimal("10000.00"),
                new BigDecimal("5000.00"),
                SpacecraftStatus.IN_TRANSIT,
                "Mars Orbit",
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(spacecraftService.updateSpacecraftStatus(1L, SpacecraftStatus.IN_TRANSIT))
                .thenReturn(Mono.just(updatedResponse));

        webTestClient.put().uri(uriBuilder -> uriBuilder.path("/api/spacecrafts/1/status").queryParam("status", "IN_TRANSIT").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.status").isEqualTo("IN_TRANSIT");

        verify(spacecraftService).updateSpacecraftStatus(1L, SpacecraftStatus.IN_TRANSIT);
    }

    @Test
    @DisplayName("GET /api/spacecrafts/{id}/exists - проверка существования")
    void spacecraftExists() {
        when(spacecraftService.spacecraftExists(1L)).thenReturn(Mono.just(true));

        webTestClient.get().uri("/api/spacecrafts/1/exists")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("true");

        verify(spacecraftService).spacecraftExists(1L);
    }

    @Test
    @DisplayName("GET /api/spacecrafts/scroll - прокрутка списка")
    void getSpacecraftsScroll() {
        when(spacecraftService.getSpacecraftsScroll(anyInt(), anyInt()))
                .thenReturn(Mono.just(List.of(spacecraftResponse)));

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/api/spacecrafts/scroll").queryParam("page", "0").queryParam("size", "20").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].registryCode").isEqualTo("SC-001");

        verify(spacecraftService).getSpacecraftsScroll(0, 20);
    }
}
