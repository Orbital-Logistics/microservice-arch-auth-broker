package org.orbitalLogistic.inventory.exceptions;

public class CargoServiceException extends RuntimeException{
    public CargoServiceException(String message) {
        super(message);
    }

    public CargoServiceException(String message, Throwable t) {
        super(message, t);
    }
}
