package org.orbitalLogistic.inventory.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.inventory.TestcontainersConfiguration;
import org.orbitalLogistic.inventory.clients.CargoServiceClient;
import org.orbitalLogistic.inventory.clients.SpacecraftServiceClient;
import org.orbitalLogistic.inventory.clients.UserServiceClient;
import org.orbitalLogistic.inventory.dto.request.CargoManifestRequestDTO;
import org.orbitalLogistic.inventory.entities.enums.ManifestPriority;
import org.orbitalLogistic.inventory.entities.enums.ManifestStatus;
import org.orbitalLogistic.inventory.repositories.CargoManifestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
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
class CargoManifestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CargoManifestRepository cargoManifestRepository;

    @MockitoBean
    private UserServiceClient userServiceClient;

    @MockitoBean
    private CargoServiceClient cargoServiceClient;

    @MockitoBean
    private SpacecraftServiceClient spacecraftServiceClient;

    @BeforeEach
    void setUp() {
        cargoManifestRepository.deleteAll();

        when(userServiceClient.userExists(anyLong())).thenReturn(true);
        when(cargoServiceClient.cargoExists(anyLong())).thenReturn(true);
        when(cargoServiceClient.storageUnitExists(anyLong())).thenReturn(true);
        when(spacecraftServiceClient.spacecraftExists(anyLong())).thenReturn(true);
    }

    @Test
    @DisplayName("Интеграционный тест: создание, получение и обновление манифеста")
    void cargoManifestLifecycle_Integration() throws Exception {
        LocalDateTime loadedAt = LocalDateTime.of(2025, 12, 10, 10, 0);

        CargoManifestRequestDTO request = new CargoManifestRequestDTO(
                1L, 1L, 1L, 100,
                loadedAt, null, 1L, null,
                ManifestStatus.PENDING,
                ManifestPriority.NORMAL
        );

        String createdResponse = mockMvc.perform(post("/api/cargo-manifests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.spacecraftId", is(1)))
                .andExpect(jsonPath("$.cargoId", is(1)))
                .andExpect(jsonPath("$.quantity", is(100)))
                .andExpect(jsonPath("$.manifestStatus", is("PENDING")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long manifestId = objectMapper.readTree(createdResponse).get("id").asLong();

        mockMvc.perform(get("/api/cargo-manifests/" + manifestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(manifestId.intValue())))
                .andExpect(jsonPath("$.quantity", is(100)))
                .andExpect(jsonPath("$.manifestStatus", is("PENDING")));

        mockMvc.perform(get("/api/cargo-manifests")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(manifestId.intValue())));

        CargoManifestRequestDTO updateRequest = new CargoManifestRequestDTO(
                1L, 1L, 1L, 100,
                loadedAt, null, 1L, null,
                ManifestStatus.LOADED,
                ManifestPriority.HIGH
        );

        mockMvc.perform(put("/api/cargo-manifests/" + manifestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(100)))
                .andExpect(jsonPath("$.manifestStatus", is("LOADED")))
                .andExpect(jsonPath("$.priority", is("HIGH")));
    }

    @Test
    @DisplayName("Интеграционный тест: получение манифестов по кораблю")
    void getManifestsBySpacecraft_Integration() throws Exception {
        LocalDateTime loadedAt = LocalDateTime.now();

        CargoManifestRequestDTO manifest1 = new CargoManifestRequestDTO(
                1L, 1L, 1L, 100,
                loadedAt, null, 1L, null,
                ManifestStatus.LOADED,
                ManifestPriority.NORMAL
        );

        CargoManifestRequestDTO manifest2 = new CargoManifestRequestDTO(
                1L, 2L, 2L, 200,
                loadedAt.plusHours(1), null, 1L, null,
                ManifestStatus.IN_TRANSIT,
                ManifestPriority.HIGH
        );

        CargoManifestRequestDTO manifest3 = new CargoManifestRequestDTO(
                2L, 3L, 3L, 150,
                loadedAt.plusHours(2), null, 1L, null,
                ManifestStatus.LOADED,
                ManifestPriority.NORMAL
        );

        mockMvc.perform(post("/api/cargo-manifests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(manifest1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/cargo-manifests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(manifest2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/cargo-manifests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(manifest3)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/cargo-manifests/spacecraft/1")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));

        mockMvc.perform(get("/api/cargo-manifests/spacecraft/2")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - корабль не найден")
    void createManifest_SpacecraftNotFound_ReturnsBadRequest() throws Exception {
        when(spacecraftServiceClient.spacecraftExists(999L)).thenReturn(false);

        LocalDateTime loadedAt = LocalDateTime.now();

        CargoManifestRequestDTO request = new CargoManifestRequestDTO(
                999L, 1L, 1L, 100,
                loadedAt, null, 1L, null,
                ManifestStatus.PENDING,
                ManifestPriority.NORMAL
        );

        mockMvc.perform(post("/api/cargo-manifests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - невалидные данные")
    void createManifest_InvalidData_ReturnsBadRequest() throws Exception {
        String invalidJson = """
                {
                    "spacecraftId": 1,
                    "cargoId": null,
                    "storageUnitId": 1,
                    "quantity": 100,
                    "loadedByUserId": 1
                }
                """;

        mockMvc.perform(post("/api/cargo-manifests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Интеграционный тест: валидация - отрицательное количество")
    void createManifest_NegativeQuantity_ReturnsBadRequest() throws Exception {
        String invalidJson = """
                {
                    "spacecraftId": 1,
                    "cargoId": 1,
                    "storageUnitId": 1,
                    "quantity": -10,
                    "loadedByUserId": 1
                }
                """;

        mockMvc.perform(post("/api/cargo-manifests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Интеграционный тест: пагинация работает корректно")
    void pagination_WorksCorrectly() throws Exception {
        LocalDateTime loadedAt = LocalDateTime.now();

        for (int i = 1; i <= 5; i++) {
            CargoManifestRequestDTO request = new CargoManifestRequestDTO(
                    1L, (long) i, (long) i, 100 * i,
                    loadedAt.plusHours(i), null, 1L, null,
                    ManifestStatus.PENDING,
                    ManifestPriority.NORMAL
            );

            mockMvc.perform(post("/api/cargo-manifests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/cargo-manifests")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.totalPages", is(3)));

        mockMvc.perform(get("/api/cargo-manifests")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(5)));

        mockMvc.perform(get("/api/cargo-manifests")
                        .param("page", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(5)));
    }
}

