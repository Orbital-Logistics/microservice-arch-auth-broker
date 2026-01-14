package org.orbitalLogistic.mission.domain.exception;

public class MissionSpacecraftExistsException extends RuntimeException {
    public MissionSpacecraftExistsException(String message) {
        super(message);
    }
}
