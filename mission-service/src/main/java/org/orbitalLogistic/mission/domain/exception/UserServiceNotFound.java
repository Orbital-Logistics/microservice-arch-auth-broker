package org.orbitalLogistic.mission.domain.exception;

public class UserServiceNotFound extends RuntimeException{
    public UserServiceNotFound(String message) {
        super(message);
    }
}
