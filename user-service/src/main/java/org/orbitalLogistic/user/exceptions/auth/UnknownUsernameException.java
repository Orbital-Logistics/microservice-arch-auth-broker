package org.orbitalLogistic.user.exceptions.auth;

public class UnknownUsernameException extends RuntimeException {
    public UnknownUsernameException(String message) {
        super(message);
    }
}
