package org.orbitalLogistic.inventory.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserDTO getUserById(Long id) {
        log.warn("Fallback: Unable to fetch user with id: {}", id);
        return null;
    }

    @Override
    public Boolean userExists(Long id) {
        log.warn("Fallback: Unable to check if user exists with id: {}", id);
        return false;
    }
}

