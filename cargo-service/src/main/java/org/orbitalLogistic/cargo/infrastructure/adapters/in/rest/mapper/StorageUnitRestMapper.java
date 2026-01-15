package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.mapper;

import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateStorageUnitRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.UpdateStorageUnitRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response.StorageUnitResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class StorageUnitRestMapper {
    
    public StorageUnitResponse toResponse(StorageUnit unit) {
        if (unit == null) {
            return null;
        }
        
        BigDecimal availableMass = unit.getMaxMass().subtract(unit.getCurrentMass());
        BigDecimal availableVolume = unit.getMaxVolume().subtract(unit.getCurrentVolume());
        
        double massUsagePercentage = unit.getCurrentMass()
                .divide(unit.getMaxMass(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        
        double volumeUsagePercentage = unit.getCurrentVolume()
                .divide(unit.getMaxVolume(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        
        return StorageUnitResponse.builder()
                .id(unit.getId())
                .unitCode(unit.getUnitCode())
                .location(unit.getLocation())
                .storageType(unit.getStorageType())
                .totalMassCapacity(unit.getMaxMass())
                .totalVolumeCapacity(unit.getMaxVolume())
                .currentMass(unit.getCurrentMass())
                .currentVolume(unit.getCurrentVolume())
                .availableMassCapacity(availableMass)
                .availableVolumeCapacity(availableVolume)
                .massUsagePercentage(massUsagePercentage)
                .volumeUsagePercentage(volumeUsagePercentage)
                .build();
    }
    
    public StorageUnit toDomain(CreateStorageUnitRequest request) {
        if (request == null) {
            return null;
        }
        
        return StorageUnit.builder()
                .unitCode(request.getUnitCode())
                .location(request.getLocation())
                .storageType(request.getStorageType())
                .maxMass(request.getTotalMassCapacity())
                .maxVolume(request.getTotalVolumeCapacity())
                .currentMass(BigDecimal.ZERO)
                .currentVolume(BigDecimal.ZERO)
                .isActive(true)
                .build();
    }
    
    public StorageUnit toDomain(UpdateStorageUnitRequest request, Long id) {
        if (request == null) {
            return null;
        }
        
        return StorageUnit.builder()
                .id(id)
                .unitCode(request.getUnitCode())
                .location(request.getLocation())
                .storageType(request.getStorageType())
                .maxMass(request.getTotalMassCapacity())
                .maxVolume(request.getTotalVolumeCapacity())
                .build();
    }
}
