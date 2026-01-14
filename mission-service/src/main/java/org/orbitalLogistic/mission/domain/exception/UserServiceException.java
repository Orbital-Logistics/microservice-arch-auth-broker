package org.orbitalLogistic.mission.domain.exception;

public class UserServiceException extends RuntimeException{
    public UserServiceException(String message) {
        super(message);
    }

    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
