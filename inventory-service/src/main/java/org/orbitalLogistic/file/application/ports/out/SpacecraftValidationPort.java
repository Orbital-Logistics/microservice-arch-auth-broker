package org.orbitalLogistic.file.application.ports.out;

public interface SpacecraftValidationPort {
    boolean spacecraftExists(Long spacecraftId);
}
