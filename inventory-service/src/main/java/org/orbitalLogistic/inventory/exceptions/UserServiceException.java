package org.orbitalLogistic.inventory.exceptions;

public class UserServiceException extends RuntimeException{
    public UserServiceException(String message) {
        super(message);
    }

    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
