package org.orbitalLogistic.inventory.infrastructure.adapters.out.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.inventory.application.ports.out.CargoValidationPort;
import org.orbitalLogistic.inventory.clients.resilient.ResilientCargoServiceClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CargoValidationAdapter implements CargoValidationPort {

    private final ResilientCargoServiceClient cargoServiceClient;

    @Override
    public boolean cargoExists(Long cargoId) {
        try {
            return cargoServiceClient.cargoExists(cargoId);
        } catch (Exception e) {
            log.error("Error validating cargo existence for id: {}", cargoId, e);
            return false;
        }
    }

    @Override
    public boolean storageUnitExists(Long storageUnitId) {
        try {
            return cargoServiceClient.storageUnitExists(storageUnitId);
        } catch (Exception e) {
            log.error("Error validating storage unit existence for id: {}", storageUnitId, e);
            return false;
        }
    }
}
