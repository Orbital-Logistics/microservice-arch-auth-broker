package org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto;

public record UpdateUserRequest(
        Long id,
        String username,
        String email
) {
}
