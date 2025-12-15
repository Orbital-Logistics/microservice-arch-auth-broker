package org.orbitalLogistic.cargo.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public String getUserById(Long id) {
        log.warn("Fallback: Unable to fetch username for user id: {}", id);
        return "Unknown User";
    }
}

