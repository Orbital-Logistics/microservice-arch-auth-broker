package org.orbitalLogistic.cargo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.orbitalLogistic.cargo.domain.model.enums.StorageTypeEnum;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageUnit {
    private Long id;
    private String unitCode;
    private String location;
    private StorageTypeEnum storageType;
    private BigDecimal maxMass;
    private BigDecimal maxVolume;
    private BigDecimal currentMass;
    private BigDecimal currentVolume;
    private Boolean isActive;
}
