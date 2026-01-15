package org.orbitalLogistic.cargo.infrastructure.adapters.out.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.out.UserServicePort;
import org.orbitalLogistic.cargo.clients.ResilientUserService;
import org.orbitalLogistic.cargo.domain.exception.UserNotFoundException;
import org.orbitalLogistic.cargo.domain.exception.UserServiceException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceAdapter implements UserServicePort {

    private final ResilientUserService resilientUserService;

    @Override
    public boolean userExists(Long userId) {
        try {
            Boolean exists = resilientUserService.userExists(userId);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Failed to check user existence for userId {}: {}", userId, e.getMessage());
            throw new UserServiceException("User service is unavailable: " + e.getMessage());
        }
    }

    @Override
    public String getUserById(Long userId) {
        try {
            String username = resilientUserService.getUserById(userId);
            if (username == null) {
                throw new UserNotFoundException("User not found with id: " + userId);
            }
            return username;
        } catch (Exception e) {
            log.error("Failed to get user by id {}: {}", userId, e.getMessage());
            throw new UserServiceException("User service is unavailable: " + e.getMessage());
        }
    }
}
