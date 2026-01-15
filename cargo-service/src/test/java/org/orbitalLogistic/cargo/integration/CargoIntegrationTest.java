package org.orbitalLogistic.cargo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.TestcontainersConfiguration;
import org.orbitalLogistic.cargo.application.ports.out.CargoCategoryRepository;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.CargoCategory;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateCargoRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.UpdateCargoRequest;
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
        CreateCargoRequest createRequest = CreateCargoRequest.builder()
                .name("Integration Test Cargo")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(BigDecimal.valueOf(1.00))
                .volumePerUnit(BigDecimal.valueOf(0.05))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build();

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

        UpdateCargoRequest updateRequest = UpdateCargoRequest.builder()
                .name("Updated Integration Test Cargo")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(BigDecimal.valueOf(1.50))
                .volumePerUnit(BigDecimal.valueOf(0.06))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.LOW)
                .build();

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
                .massPerUnit(BigDecimal.valueOf(1.00))
                .volumePerUnit(BigDecimal.valueOf(0.05))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .isActive(true)
                .build());

        Cargo cargo2 = cargoRepository.save(Cargo.builder()
                .name("Search Test Cargo 2")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(BigDecimal.valueOf(2.00))
                .volumePerUnit(BigDecimal.valueOf(0.10))
                .cargoType(CargoType.FOOD)
                .hazardLevel(HazardLevel.LOW)
                .isActive(true)
                .build());

        mockMvc.perform(get("/api/cargos/search")
                        .param("name", "Search Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/cargos/search")
                        .param("cargoType", "EQUIPMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Search Test Cargo 1"));
    }

    @Test
    void createCargo_DuplicateName_ReturnsError() throws Exception {
        cargoRepository.save(Cargo.builder()
                .name("Duplicate Cargo")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(BigDecimal.valueOf(1.00))
                .volumePerUnit(BigDecimal.valueOf(0.05))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .isActive(true)
                .build());

        CreateCargoRequest duplicateRequest = CreateCargoRequest.builder()
                .name("Duplicate Cargo")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(BigDecimal.valueOf(1.00))
                .volumePerUnit(BigDecimal.valueOf(0.05))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build();

        mockMvc.perform(post("/api/cargos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }
}

