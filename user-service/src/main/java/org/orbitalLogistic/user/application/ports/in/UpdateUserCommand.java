package org.orbitalLogistic.user.application.ports.in;

public record UpdateUserCommand(
        Long id,
        String username,
        String email
) {
}
