package org.orbitalLogistic.file.application.ports.out;

public interface UserValidationPort {
    boolean userExists(Long userId);
}
