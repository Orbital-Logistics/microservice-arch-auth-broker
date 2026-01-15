package org.orbitalLogistic.cargo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cargo {
    private Long id;
    private String name;
    private Long cargoCategoryId;
    private BigDecimal massPerUnit;
    private BigDecimal volumePerUnit;
    private CargoType cargoType;
    private HazardLevel hazardLevel;
    @Builder.Default
    private Boolean isActive = true;
}
