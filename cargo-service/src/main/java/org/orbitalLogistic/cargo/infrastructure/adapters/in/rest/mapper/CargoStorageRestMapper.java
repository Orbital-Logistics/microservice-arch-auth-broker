package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.mapper;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.application.ports.out.UserServicePort;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateCargoStorageRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response.CargoStorageResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CargoStorageRestMapper {
    
    private final StorageUnitRepository storageUnitRepository;
    private final CargoRepository cargoRepository;
    private final UserServicePort userServicePort;
    
    public CargoStorageResponse toResponse(CargoStorage cargoStorage) {
        if (cargoStorage == null) {
            return null;
        }
        
        String storageUnitCode = null;
        String storageLocation = null;
        if (cargoStorage.getStorageUnitId() != null) {
            storageUnitRepository.findById(cargoStorage.getStorageUnitId()).ifPresent(unit -> {});
            StorageUnit unit = storageUnitRepository.findById(cargoStorage.getStorageUnitId()).orElse(null);
            if (unit != null) {
                storageUnitCode = unit.getUnitCode();
                storageLocation = unit.getLocation();
            }
        }
        
        String cargoName = null;
        if (cargoStorage.getCargoId() != null) {
            cargoName = cargoRepository.findById(cargoStorage.getCargoId())
                    .map(Cargo::getName)
                    .orElse(null);
        }
        
        String lastCheckedByUserName = null;
        if (cargoStorage.getLastCheckedByUserId() != null) {
            lastCheckedByUserName = userServicePort.getUserById(cargoStorage.getLastCheckedByUserId());
        }
        
        return CargoStorageResponse.builder()
                .id(cargoStorage.getId())
                .storageUnitId(cargoStorage.getStorageUnitId())
                .storageUnitCode(storageUnitCode)
                .storageLocation(storageLocation)
                .cargoId(cargoStorage.getCargoId())
                .cargoName(cargoName)
                .quantity(cargoStorage.getQuantity())
                .storedAt(cargoStorage.getStoredAt())
                .lastInventoryCheck(cargoStorage.getLastInventoryCheck())
                .lastCheckedByUserId(cargoStorage.getLastCheckedByUserId())
                .lastCheckedByUserName(lastCheckedByUserName)
                .build();
    }
    
    public CargoStorage toDomain(CreateCargoStorageRequest request) {
        if (request == null) {
            return null;
        }
        
        return CargoStorage.builder()
                .storageUnitId(request.getStorageUnitId())
                .cargoId(request.getCargoId())
                .quantity(request.getQuantity())
                .storedAt(LocalDateTime.now())
                .lastCheckedByUserId(request.getUpdatedByUserId())
                .lastInventoryCheck(LocalDateTime.now())
                .build();
    }
}
