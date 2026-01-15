package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoResponse {
    
    private Long id;
    private String name;
    private Long cargoCategoryId;
    private String cargoCategoryName;
    private BigDecimal massPerUnit;
    private BigDecimal volumePerUnit;
    private CargoType cargoType;
    private HazardLevel hazardLevel;
    private Boolean isActive;
    private Integer totalQuantity;
}
