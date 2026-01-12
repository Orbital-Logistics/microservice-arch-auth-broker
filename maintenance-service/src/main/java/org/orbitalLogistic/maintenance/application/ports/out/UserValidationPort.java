package org.orbitalLogistic.maintenance.application.ports.out;

import reactor.core.publisher.Mono;

public interface UserValidationPort {
    Mono<Boolean> userExists(Long userId);
}
