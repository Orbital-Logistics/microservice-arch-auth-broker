package org.orbitalLogistic.cargo.domain.exception;

public class CargoCategoryNotFoundException extends RuntimeException {
    public CargoCategoryNotFoundException(String message) {
        super(message);
    }
}
