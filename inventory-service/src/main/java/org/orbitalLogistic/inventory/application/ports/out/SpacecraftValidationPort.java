package org.orbitalLogistic.inventory.application.ports.out;

public interface SpacecraftValidationPort {
    boolean spacecraftExists(Long spacecraftId);
}
