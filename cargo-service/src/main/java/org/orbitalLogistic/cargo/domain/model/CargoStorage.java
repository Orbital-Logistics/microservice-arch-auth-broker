package org.orbitalLogistic.cargo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoStorage {
    private Long id;
    private Long storageUnitId;
    private Long cargoId;
    private Integer quantity;
    @Builder.Default
    private LocalDateTime storedAt = LocalDateTime.now();
    private LocalDateTime lastInventoryCheck;
    private Long lastCheckedByUserId;
    private Long responsibleUserId;
}
