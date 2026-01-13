package org.orbitalLogistic.user.application.ports.in;

public record LoginCommand(
        String username,
        String password
) {
}
