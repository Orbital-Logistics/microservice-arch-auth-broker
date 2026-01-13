package org.orbitalLogistic.user.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Role {
    Long id;
    String name;

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name is required");
        }
        if (name.length() < 2 || name.length() > 50) {
            throw new IllegalArgumentException("Role name must be between 2 and 50 characters");
        }
        if (!name.matches("^[A-Z_]+$")) {
            throw new IllegalArgumentException("Role name must be uppercase letters and underscores only");
        }
    }

    public static Role create(String name) {
        Role role = Role.builder()
                .name(name.toUpperCase())
                .build();
        role.validate();
        return role;
    }
}
