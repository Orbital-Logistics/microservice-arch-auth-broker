package org.orbitalLogistic.cargo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.TestcontainersConfiguration;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.orbitalLogistic.cargo.domain.model.enums.StorageTypeEnum;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateStorageUnitRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.UpdateStorageUnitRequest;
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
        CreateStorageUnitRequest createRequest = CreateStorageUnitRequest.builder()
                .unitCode("INT-TEST-SU-001")
                .location("Integration Test Warehouse")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(BigDecimal.valueOf(20000.00))
                .totalVolumeCapacity(BigDecimal.valueOf(1000.00))
                .build();

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

        UpdateStorageUnitRequest updateRequest = UpdateStorageUnitRequest.builder()
                .unitCode("INT-TEST-SU-UPD")
                .location("Updated Integration Test Warehouse")
                .storageType(StorageTypeEnum.PRESSURIZED)
                .totalMassCapacity(BigDecimal.valueOf(25000.00))
                .totalVolumeCapacity(BigDecimal.valueOf(1200.00))
                .build();

        mockMvc.perform(put("/api/storage-units/{id}", unitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitCode").value("INT-TEST-SU-UPD"))
                .andExpect(jsonPath("$.location").value("Updated Integration Test Warehouse"));
    }

    @Test
    void getAllStorageUnits_Integration() throws Exception {
        storageUnitRepository.save(StorageUnit.builder()
                .unitCode("INT-SU-001")
                .location("Test Location 1")
                .storageType(StorageTypeEnum.AMBIENT)
                .maxMass(BigDecimal.valueOf(10000.00))
                .maxVolume(BigDecimal.valueOf(500.00))
                .currentMass(BigDecimal.ZERO)
                .currentVolume(BigDecimal.ZERO)
                .isActive(true)
                .build());

        storageUnitRepository.save(StorageUnit.builder()
                .unitCode("INT-SU-002")
                .location("Test Location 2")
                .storageType(StorageTypeEnum.PRESSURIZED)
                .maxMass(BigDecimal.valueOf(15000.00))
                .maxVolume(BigDecimal.valueOf(700.00))
                .currentMass(BigDecimal.ZERO)
                .currentVolume(BigDecimal.ZERO)
                .isActive(true)
                .build());

        mockMvc.perform(get("/api/storage-units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createStorageUnit_DuplicateCode_ReturnsError() throws Exception {
        storageUnitRepository.save(StorageUnit.builder()
                .unitCode("DUPLICATE-SU-001")
                .location("Duplicate Location")
                .storageType(StorageTypeEnum.AMBIENT)
                .maxMass(BigDecimal.valueOf(10000.00))
                .maxVolume(BigDecimal.valueOf(500.00))
                .currentMass(BigDecimal.ZERO)
                .currentVolume(BigDecimal.ZERO)
                .isActive(true)
                .build());

        CreateStorageUnitRequest duplicateRequest = CreateStorageUnitRequest.builder()
                .unitCode("DUPLICATE-SU-001")
                .location("Another Location")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(BigDecimal.valueOf(10000.00))
                .totalVolumeCapacity(BigDecimal.valueOf(500.00))
                .build();

        mockMvc.perform(post("/api/storage-units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }
}

