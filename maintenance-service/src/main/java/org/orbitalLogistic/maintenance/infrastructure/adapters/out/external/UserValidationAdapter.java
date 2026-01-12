package org.orbitalLogistic.maintenance.infrastructure.adapters.out.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.maintenance.application.ports.out.UserValidationPort;
import org.orbitalLogistic.maintenance.clients.UserServiceClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserValidationAdapter implements UserValidationPort {

    private final UserServiceClient userServiceClient;

    @Override
    public Mono<Boolean> userExists(Long userId) {
        return userServiceClient.userExists(userId);
    }
}
