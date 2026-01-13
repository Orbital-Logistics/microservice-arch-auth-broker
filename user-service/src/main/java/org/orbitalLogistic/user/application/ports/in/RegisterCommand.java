package org.orbitalLogistic.user.application.ports.in;

import java.util.Set;

public record RegisterCommand(
        String username,
        String password,
        String email,
        Set<Long> roleIds
) {
}
