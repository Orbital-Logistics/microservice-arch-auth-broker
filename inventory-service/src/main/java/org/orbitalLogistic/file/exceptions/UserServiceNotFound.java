package org.orbitalLogistic.file.exceptions;

public class UserServiceNotFound extends RuntimeException{
    public UserServiceNotFound(String message) {
        super(message);
    }

    public UserServiceNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
