package org.orbitalLogistic.inventory.infrastructure.adapters.out.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.inventory.application.ports.out.UserValidationPort;
import org.orbitalLogistic.inventory.clients.resilient.ResilientUserService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserValidationAdapter implements UserValidationPort {

    private final ResilientUserService userService;

    @Override
    public boolean userExists(Long userId) {
        try {
            return userService.userExists(userId);
        } catch (Exception e) {
            log.error("Error validating user existence for id: {}", userId, e);
            return false;
        }
    }
}
