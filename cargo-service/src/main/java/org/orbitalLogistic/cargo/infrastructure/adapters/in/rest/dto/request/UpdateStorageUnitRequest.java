package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.orbitalLogistic.cargo.domain.model.enums.StorageTypeEnum;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStorageUnitRequest {
    
    @NotBlank(message = "Unit code is required")
    @Size(max = 20, message = "Unit code must not exceed 20 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Unit code can only contain uppercase letters, numbers and hyphens")
    private String unitCode;
    
    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;
    
    @NotNull(message = "Storage type is required")
    private StorageTypeEnum storageType;
    
    @NotNull(message = "Total mass capacity is required")
    @DecimalMin(value = "0.01", message = "Total mass capacity must be positive")
    @Digits(integer = 13, fraction = 2, message = "Total mass capacity format invalid")
    private BigDecimal totalMassCapacity;
    
    @NotNull(message = "Total volume capacity is required")
    @DecimalMin(value = "0.01", message = "Total volume capacity must be positive")
    @Digits(integer = 13, fraction = 2, message = "Total volume capacity format invalid")
    private BigDecimal totalVolumeCapacity;
}
