package org.orbitalLogistic.inventory.application.ports.out;

public interface CargoValidationPort {
    boolean cargoExists(Long cargoId);
    boolean storageUnitExists(Long storageUnitId);
}
