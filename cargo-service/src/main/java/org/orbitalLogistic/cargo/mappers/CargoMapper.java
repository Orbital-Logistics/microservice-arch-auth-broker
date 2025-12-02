package org.orbitalLogistic.cargo.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.cargo.dto.request.CargoRequestDTO;
import org.orbitalLogistic.cargo.dto.response.CargoResponseDTO;
import org.orbitalLogistic.cargo.entities.Cargo;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CargoMapper {

    @Mapping(target = "cargoCategoryName", source = "cargoCategoryName")
    @Mapping(target = "totalQuantity", source = "totalQuantity")
    CargoResponseDTO toResponseDTO(
            Cargo cargo,
            String cargoCategoryName,
            Integer totalQuantity
    );
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Cargo toEntity(CargoRequestDTO request);

}





