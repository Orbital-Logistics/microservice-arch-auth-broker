package org.orbitalLogistic.cargo.domain.exception;

public class CargoAlreadyExistsException extends RuntimeException {
    public CargoAlreadyExistsException(String message) {
        super(message);
    }
}
