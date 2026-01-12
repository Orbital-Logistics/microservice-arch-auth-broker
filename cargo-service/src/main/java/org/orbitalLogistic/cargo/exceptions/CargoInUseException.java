package org.orbitalLogistic.cargo.exceptions;

public class CargoInUseException extends RuntimeException {
    public CargoInUseException(String message) {
        super(message);
    }
}
