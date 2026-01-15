package org.orbitalLogistic.cargo.application.ports.out;

public interface UserServicePort {
    boolean userExists(Long userId);
    String getUserById(Long userId);
}
