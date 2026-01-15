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
public class UpdateInventoryRequest {
    
    @NotNull(message = "New quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer newQuantity;
}
