package org.orbitalLogistic.user.application.ports.in;

import java.util.Set;

public record CreateUserCommand(
        String username,
        String password,
        String email,
        Set<Long> roleIds
) {
}
