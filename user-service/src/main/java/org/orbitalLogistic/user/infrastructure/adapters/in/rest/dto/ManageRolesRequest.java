package org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto;

import java.util.Set;

public record ManageRolesRequest(
        Long userId,
        Set<Long> roleIds
) {
}
