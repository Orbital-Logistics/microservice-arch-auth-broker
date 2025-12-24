package org.orbitalLogistic.cargo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.TestcontainersConfiguration;
import org.orbitalLogistic.cargo.dto.request.StorageUnitRequestDTO;
import org.orbitalLogistic.cargo.entities.StorageUnit;
import org.orbitalLogistic.cargo.entities.enums.StorageTypeEnum;
import org.orbitalLogistic.cargo.repositories.StorageUnitRepository;
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
class StorageUnitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StorageUnitRepository storageUnitRepository;

    @Test
    void storageUnitLifecycle_Integration() throws Exception {
        StorageUnitRequestDTO createRequest = new StorageUnitRequestDTO(
                "INT-TEST-SU-001",
                "Integration Test Warehouse",
                StorageTypeEnum.AMBIENT,
                new BigDecimal("20000.00"),
                new BigDecimal("1000.00")
        );

        String createResponse = mockMvc.perform(post("/api/storage-units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitCode").value("INT-TEST-SU-001"))
                .andExpect(jsonPath("$.location").value("Integration Test Warehouse"))
                .andReturn().getResponse().getContentAsString();

        Long unitId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/storage-units/{id}", unitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(unitId))
                .andExpect(jsonPath("$.unitCode").value("INT-TEST-SU-001"));

        StorageUnitRequestDTO updateRequest = new StorageUnitRequestDTO(
                "INT-TEST-SU-UPD",
                "Updated Integration Test Warehouse",
                StorageTypeEnum.PRESSURIZED,
                new BigDecimal("25000.00"),
                new BigDecimal("1200.00")
        );

        mockMvc.perform(put("/api/storage-units/{id}", unitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitCode").value("INT-TEST-SU-UPD"))
                .andExpect(jsonPath("$.location").value("Updated Integration Test Warehouse"));

        mockMvc.perform(get("/api/storage-units/{id}/exists", unitId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void getAllStorageUnits_Integration() throws Exception {
        storageUnitRepository.save(StorageUnit.builder()
                .unitCode("INT-SU-001")
                .location("Test Location 1")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(new BigDecimal("10000.00"))
                .totalVolumeCapacity(new BigDecimal("500.00"))
                .currentMass(new BigDecimal("0.00"))
                .currentVolume(new BigDecimal("0.00"))
                .build());

        storageUnitRepository.save(StorageUnit.builder()
                .unitCode("INT-SU-002")
                .location("Test Location 2")
                .storageType(StorageTypeEnum.PRESSURIZED)
                .totalMassCapacity(new BigDecimal("15000.00"))
                .totalVolumeCapacity(new BigDecimal("700.00"))
                .currentMass(new BigDecimal("0.00"))
                .currentVolume(new BigDecimal("0.00"))
                .build());

        mockMvc.perform(get("/api/storage-units")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createStorageUnit_DuplicateCode_ReturnsError() throws Exception {
        storageUnitRepository.save(StorageUnit.builder()
                .unitCode("DUPLICATE-SU-001")
                .location("Duplicate Location")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(new BigDecimal("10000.00"))
                .totalVolumeCapacity(new BigDecimal("500.00"))
                .currentMass(new BigDecimal("0.00"))
                .currentVolume(new BigDecimal("0.00"))
                .build());

        StorageUnitRequestDTO duplicateRequest = new StorageUnitRequestDTO(
                "DUPLICATE-SU-001",
                "Another Location",
                StorageTypeEnum.AMBIENT,
                new BigDecimal("10000.00"),
                new BigDecimal("500.00")
        );

        mockMvc.perform(post("/api/storage-units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void getStorageUnitInventory_Integration() throws Exception {
        StorageUnit unit = storageUnitRepository.save(StorageUnit.builder()
                .unitCode("INVENTORY-SU-001")
                .location("Inventory Location")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(new BigDecimal("10000.00"))
                .totalVolumeCapacity(new BigDecimal("500.00"))
                .currentMass(new BigDecimal("0.00"))
                .currentVolume(new BigDecimal("0.00"))
                .build());

        mockMvc.perform(get("/api/storage-units/{id}/inventory", unit.getId())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"));
    }
}

