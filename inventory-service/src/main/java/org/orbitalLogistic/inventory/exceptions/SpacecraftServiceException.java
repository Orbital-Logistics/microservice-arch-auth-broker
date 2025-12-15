package org.orbitalLogistic.inventory.exceptions;

public class SpacecraftServiceException extends RuntimeException {
    public SpacecraftServiceException(String message) {
        super(message);
    }

    public SpacecraftServiceException(String message, Throwable t) {
        super(message, t);
    }
}
