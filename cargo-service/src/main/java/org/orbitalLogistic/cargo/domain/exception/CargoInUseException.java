package org.orbitalLogistic.cargo.domain.exception;

public class CargoInUseException extends RuntimeException {
    public CargoInUseException(String message) {
        super(message);
    }
}
