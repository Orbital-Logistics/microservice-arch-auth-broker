package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoStorageResponse {
    
    private Long id;
    private Long storageUnitId;
    private String storageUnitCode;
    private String storageLocation;
    private Long cargoId;
    private String cargoName;
    private Integer quantity;
    private LocalDateTime storedAt;
    private LocalDateTime lastInventoryCheck;
    private Long lastCheckedByUserId;
    private String lastCheckedByUserName;
}
