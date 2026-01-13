package org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto;

public record ChangePasswordRequest(
        Long userId,
        String newPassword
) {
}
