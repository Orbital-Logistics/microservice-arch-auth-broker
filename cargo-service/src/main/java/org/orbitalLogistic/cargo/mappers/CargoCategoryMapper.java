package org.orbitalLogistic.cargo.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.cargo.dto.request.CargoCategoryRequestDTO;
import org.orbitalLogistic.cargo.dto.response.CargoCategoryResponseDTO;
import org.orbitalLogistic.cargo.entities.CargoCategory;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CargoCategoryMapper {

    @Mapping(target = "parentCategoryName", source = "parentCategoryName")
    @Mapping(target = "children", source = "children")
    @Mapping(target = "level", source = "level")
    CargoCategoryResponseDTO toResponseDTO(
            CargoCategory category,
            String parentCategoryName,
            List<CargoCategoryResponseDTO> children,
            Integer level
    );

    @Mapping(target = "id", ignore = true)
    CargoCategory toEntity(CargoCategoryRequestDTO request);
}

