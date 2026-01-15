package org.orbitalLogistic.cargo.domain.exception;

public class CargoStorageNotFoundException extends RuntimeException {
    public CargoStorageNotFoundException(String message) {
        super(message);
    }
}
