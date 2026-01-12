package org.orbitalLogistic.inventory.application.ports.out;

public interface UserValidationPort {
    boolean userExists(Long userId);
}
