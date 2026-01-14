package org.orbitalLogistic.mission.domain.exception;

public class MissionAlreadyExistsException extends RuntimeException {
    public MissionAlreadyExistsException(String message) {
        super(message);
    }
}

