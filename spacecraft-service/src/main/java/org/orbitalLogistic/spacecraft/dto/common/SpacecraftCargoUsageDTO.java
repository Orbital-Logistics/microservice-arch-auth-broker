package org.orbitalLogistic.spacecraft.dto.common;

import java.math.BigDecimal;

public record SpacecraftCargoUsageDTO(
        Long spacecraftId,
        BigDecimal currentMassUsage,
        BigDecimal currentVolumeUsage
) {
}

