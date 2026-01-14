package org.orbitalLogistic.mission.domain.exception;

public class SpacecraftServiceNotFound extends RuntimeException{
    public SpacecraftServiceNotFound(String message) {
        super(message);
    }
}
