package org.orbitalLogistic.inventory.exceptions;

public class UserServiceNotFound extends RuntimeException{
    public UserServiceNotFound(String message) {
        super(message);
    }

    public UserServiceNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
