package org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto;

import java.util.Set;

public record AuthResponse(
        String token,
        String username,
        Set<String> roles
) {
}
