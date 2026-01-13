package org.orbitalLogistic.spacecraft.application.ports.out;

public interface CargoValidationPort {
    boolean isSpacecraftUsedInCargo(Long spacecraftId);
}

