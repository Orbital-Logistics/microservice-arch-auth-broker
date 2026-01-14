package org.orbitalLogistic.mission.application.ports.out;

public interface SpacecraftServicePort {
    String getSpacecraftNameById(Long spacecraftId);
    boolean spacecraftExists(Long spacecraftId);
}
