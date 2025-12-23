package org.orbitalLogistic.spacecraft.clients;

import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.spacecraft.dto.common.SpacecraftCargoUsageDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class CargoServiceClientFallback implements CargoServiceClient {

    @Override
    public SpacecraftCargoUsageDTO getSpacecraftCargoUsage(Long spacecraftId) {
        log.warn("Fallback: Unable to fetch cargo usage for spacecraft with id: {}", spacecraftId);
        return new SpacecraftCargoUsageDTO(spacecraftId, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}

