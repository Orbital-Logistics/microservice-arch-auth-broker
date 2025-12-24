package org.orbitalLogistic.cargo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.TestcontainersConfiguration;
import org.orbitalLogistic.cargo.dto.request.CargoRequestDTO;
import org.orbitalLogistic.cargo.entities.Cargo;
import org.orbitalLogistic.cargo.entities.CargoCategory;
import org.orbitalLogistic.cargo.entities.enums.CargoType;
import org.orbitalLogistic.cargo.entities.enums.HazardLevel;
import org.orbitalLogistic.cargo.repositories.CargoCategoryRepository;
import org.orbitalLogistic.cargo.repositories.CargoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
@Transactional
@WithMockUser(roles = "ADMIN")
class CargoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private CargoCategoryRepository cargoCategoryRepository;

    private CargoCategory testCategory;

    @BeforeEach
    void setUp() {
        testCategory = cargoCategoryRepository.save(CargoCategory.builder()
                .name("Test Electronics")
                .description("Test electronic components")
                .build());
    }

    @Test
    void cargoLifecycle_Integration() throws Exception {
        CargoRequestDTO createRequest = new CargoRequestDTO(
                "Integration Test Cargo",
                testCategory.getId(),
                new BigDecimal("1.00"),
                new BigDecimal("0.05"),
                CargoType.EQUIPMENT,
                HazardLevel.NONE
        );

        String createResponse = mockMvc.perform(post("/api/cargos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Test Cargo"))
                .andReturn().getResponse().getContentAsString();

        Long cargoId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/cargos/{id}", cargoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cargoId))
                .andExpect(jsonPath("$.name").value("Integration Test Cargo"));

        CargoRequestDTO updateRequest = new CargoRequestDTO(
                "Updated Integration Test Cargo",
                testCategory.getId(),
                new BigDecimal("1.50"),
                new BigDecimal("0.06"),
                CargoType.EQUIPMENT,
                HazardLevel.LOW
        );

        mockMvc.perform(put("/api/cargos/{id}", cargoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Integration Test Cargo"));

        mockMvc.perform(delete("/api/cargos/{id}", cargoId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cargos/{id}", cargoId))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchCargos_Integration() throws Exception {
        Cargo cargo1 = cargoRepository.save(Cargo.builder()
                .name("Search Test Cargo 1")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("1.00"))
                .volumePerUnit(new BigDecimal("0.05"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build());

        Cargo cargo2 = cargoRepository.save(Cargo.builder()
                .name("Search Test Cargo 2")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("2.00"))
                .volumePerUnit(new BigDecimal("0.10"))
                .cargoType(CargoType.FOOD)
                .hazardLevel(HazardLevel.LOW)
                .build());

        mockMvc.perform(get("/api/cargos/search")
                        .param("name", "Search Test")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(get("/api/cargos/search")
                        .param("cargoType", "EQUIPMENT")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + cargo1.getId() + ")].name").value("Search Test Cargo 1"));
    }

    @Test
    void createCargo_DuplicateName_ReturnsError() throws Exception {
        cargoRepository.save(Cargo.builder()
                .name("Duplicate Cargo")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("1.00"))
                .volumePerUnit(new BigDecimal("0.05"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build());

        CargoRequestDTO duplicateRequest = new CargoRequestDTO(
                "Duplicate Cargo",
                testCategory.getId(),
                new BigDecimal("1.00"),
                new BigDecimal("0.05"),
                CargoType.EQUIPMENT,
                HazardLevel.NONE
        );

        mockMvc.perform(post("/api/cargos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }
}

