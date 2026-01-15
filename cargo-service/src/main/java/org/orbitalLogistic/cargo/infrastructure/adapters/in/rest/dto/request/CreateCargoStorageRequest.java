package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCargoStorageRequest {
    
    @NotNull(message = "Storage unit ID is required")
    private Long storageUnitId;
    
    @NotNull(message = "Cargo ID is required")
    private Long cargoId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity;
    
    private Long updatedByUserId;
}
