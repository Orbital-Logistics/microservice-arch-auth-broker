package org.orbitalLogistic.cargo.dto.response;

import org.orbitalLogistic.cargo.entities.enums.CargoType;
import org.orbitalLogistic.cargo.entities.enums.HazardLevel;

import java.math.BigDecimal;

public record CargoResponseDTO(
    Long id,
    String name,
    String cargoCategoryName,
    BigDecimal massPerUnit,
    BigDecimal volumePerUnit,
    CargoType cargoType,
    HazardLevel hazardLevel,
    Integer totalQuantity
) {}

