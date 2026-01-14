package org.orbitalLogistic.mission.infrastructure.adapters.out.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.application.ports.out.UserServicePort;
import org.orbitalLogistic.mission.clients.UserDTO;
import org.orbitalLogistic.mission.clients.resilient.ResilientUserService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceAdapter implements UserServicePort {

    private final ResilientUserService resilientUserService;

    @Override
    public String getUserNameById(Long userId) {
        try {
            UserDTO user = resilientUserService.getUserById(userId);
            return user != null ? user.username() : "Unknown";
        } catch (Exception e) {
            log.warn("Failed to get user name for userId {}: {}", userId, e.getMessage());
            return "Unknown";
        }
    }

    @Override
    public boolean userExists(Long userId) {
        try {
            Boolean exists = resilientUserService.userExists(userId);
            return exists != null && exists;
        } catch (Exception e) {
            log.warn("Failed to check user existence for userId {}: {}", userId, e.getMessage());
            return false;
        }
    }
}
