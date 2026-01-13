package org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto;

import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        Boolean enabled,
        Set<RoleResponse> roles
) {
}
