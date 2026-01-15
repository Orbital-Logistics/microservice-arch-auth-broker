package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.orbitalLogistic.cargo.domain.model.enums.StorageTypeEnum;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageUnitResponse {
    
    private Long id;
    private String unitCode;
    private String location;
    private StorageTypeEnum storageType;
    private BigDecimal totalMassCapacity;
    private BigDecimal totalVolumeCapacity;
    private BigDecimal currentMass;
    private BigDecimal currentVolume;
    private BigDecimal availableMassCapacity;
    private BigDecimal availableVolumeCapacity;
    private Double massUsagePercentage;
    private Double volumeUsagePercentage;
}
