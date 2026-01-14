package org.orbitalLogistic.mission.application.ports.out;

public interface UserServicePort {
    String getUserNameById(Long userId);
    boolean userExists(Long userId);
}
