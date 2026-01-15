package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request;

import jakarta.validation.constraints.*;
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
public class UpdateCargoRequest {
    
    @NotBlank(message = "Cargo name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;
    
    @NotNull(message = "Cargo category ID is required")
    private Long cargoCategoryId;
    
    @NotNull(message = "Mass per unit is required")
    @DecimalMin(value = "0.01", message = "Mass per unit must be positive")
    @Digits(integer = 8, fraction = 2, message = "Mass per unit format invalid")
    private BigDecimal massPerUnit;
    
    @NotNull(message = "Volume per unit is required")
    @DecimalMin(value = "0.01", message = "Volume per unit must be positive")
    @Digits(integer = 8, fraction = 2, message = "Volume per unit format invalid")
    private BigDecimal volumePerUnit;
    
    @NotNull(message = "Cargo type is required")
    private CargoType cargoType;
    
    @NotNull(message = "Hazard level is required")
    private HazardLevel hazardLevel;
    
    private Boolean isActive;
}
