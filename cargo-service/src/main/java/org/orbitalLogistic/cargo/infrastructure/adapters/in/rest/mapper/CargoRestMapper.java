package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.mapper;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.cargo.application.ports.out.CargoCategoryRepository;
import org.orbitalLogistic.cargo.application.ports.out.CargoStorageRepository;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.CargoCategory;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateCargoRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.UpdateCargoRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response.CargoResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CargoRestMapper {
    
    private final CargoCategoryRepository cargoCategoryRepository;
    private final CargoStorageRepository cargoStorageRepository;
    
    public CargoResponse toResponse(Cargo cargo) {
        if (cargo == null) {
            return null;
        }
        
        String categoryName = null;
        if (cargo.getCargoCategoryId() != null) {
            categoryName = cargoCategoryRepository.findById(cargo.getCargoCategoryId())
                    .map(CargoCategory::getName)
                    .orElse(null);
        }
        
        Integer totalQuantity = cargoStorageRepository.sumQuantityByCargoId(cargo.getId());
        
        return CargoResponse.builder()
                .id(cargo.getId())
                .name(cargo.getName())
                .cargoCategoryId(cargo.getCargoCategoryId())
                .cargoCategoryName(categoryName)
                .massPerUnit(cargo.getMassPerUnit())
                .volumePerUnit(cargo.getVolumePerUnit())
                .cargoType(cargo.getCargoType())
                .hazardLevel(cargo.getHazardLevel())
                .isActive(cargo.getIsActive())
                .totalQuantity(totalQuantity != null ? totalQuantity : 0)
                .build();
    }
    
    public Cargo toDomain(CreateCargoRequest request) {
        if (request == null) {
            return null;
        }
        
        return Cargo.builder()
                .name(request.getName())
                .cargoCategoryId(request.getCargoCategoryId())
                .massPerUnit(request.getMassPerUnit())
                .volumePerUnit(request.getVolumePerUnit())
                .cargoType(request.getCargoType())
                .hazardLevel(request.getHazardLevel())
                .isActive(true)
                .build();
    }
    
    public Cargo toDomain(UpdateCargoRequest request, Long id) {
        if (request == null) {
            return null;
        }
        
        return Cargo.builder()
                .id(id)
                .name(request.getName())
                .cargoCategoryId(request.getCargoCategoryId())
                .massPerUnit(request.getMassPerUnit())
                .volumePerUnit(request.getVolumePerUnit())
                .cargoType(request.getCargoType())
                .hazardLevel(request.getHazardLevel())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }
}
