package org.orbitalLogistic.cargo.domain.exception;

public class StorageUnitNotFoundException extends RuntimeException {
    public StorageUnitNotFoundException(String message) {
        super(message);
    }
}
