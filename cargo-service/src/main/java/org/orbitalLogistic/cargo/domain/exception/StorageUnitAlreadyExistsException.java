package org.orbitalLogistic.cargo.domain.exception;

public class StorageUnitAlreadyExistsException extends RuntimeException {
    public StorageUnitAlreadyExistsException(String message) {
        super(message);
    }
}
