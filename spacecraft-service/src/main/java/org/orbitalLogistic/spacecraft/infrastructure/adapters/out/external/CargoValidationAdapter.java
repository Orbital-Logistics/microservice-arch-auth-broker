package org.orbitalLogistic.spacecraft.infrastructure.adapters.out.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.spacecraft.application.ports.out.CargoValidationPort;
import org.orbitalLogistic.spacecraft.clients.ResilientCargoServiceClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CargoValidationAdapter implements CargoValidationPort {

    private final ResilientCargoServiceClient cargoServiceClient;

    @Override
    public boolean isSpacecraftUsedInCargo(Long spacecraftId) {
        try {
            var cargoUsage = cargoServiceClient.getSpacecraftCargoUsage(spacecraftId);
            return cargoUsage != null &&
                   (cargoUsage.currentMassUsage().compareTo(java.math.BigDecimal.ZERO) > 0 ||
                    cargoUsage.currentVolumeUsage().compareTo(java.math.BigDecimal.ZERO) > 0);
        } catch (Exception e) {
            log.error("Error checking spacecraft cargo usage for id: {}", spacecraftId, e);
            return false;
        }
    }
}

