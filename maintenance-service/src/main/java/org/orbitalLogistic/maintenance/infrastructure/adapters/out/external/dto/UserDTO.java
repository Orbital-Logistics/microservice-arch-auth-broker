package org.orbitalLogistic.maintenance.infrastructure.adapters.out.external.dto;

public record UserDTO(
        Long id,
        String username,
        String email
) {
}
