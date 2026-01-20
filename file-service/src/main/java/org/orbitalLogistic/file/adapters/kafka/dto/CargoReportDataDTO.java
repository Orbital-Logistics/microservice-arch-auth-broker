package org.orbitalLogistic.file.adapters.kafka.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CargoReportDataDTO(
    Long id,
    Long storageUnitId,
    Long cargoId,
    Integer quantity,
    LocalDateTime storedAt,
    LocalDateTime lastInventoryCheck,
    Long lastCheckedByUserId,
    Long responsibleUserId
) {}
