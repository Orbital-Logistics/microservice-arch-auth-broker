package org.orbitalLogistic.file.exceptions;

public class SpacecraftServiceException extends RuntimeException {
    public SpacecraftServiceException(String message) {
        super(message);
    }

    public SpacecraftServiceException(String message, Throwable t) {
        super(message, t);
    }
}
