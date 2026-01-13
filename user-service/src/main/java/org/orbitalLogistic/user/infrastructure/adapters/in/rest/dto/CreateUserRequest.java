package org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto;

import java.util.Set;

public record CreateUserRequest(
        String username,
        String password,
        String email,
        Set<Long> roleIds
) {
}
