package org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto;

public record LoginRequest(
        String username,
        String password
) {
}
