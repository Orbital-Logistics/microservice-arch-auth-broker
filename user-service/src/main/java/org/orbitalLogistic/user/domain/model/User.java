package org.orbitalLogistic.user.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder(toBuilder = true)
public class User {
    Long id;
    String username;
    String password;
    String email;
    Boolean enabled;
    Set<Role> roles;

    public void validate() {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }
        if (!username.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, numbers, underscores and hyphens");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email format is invalid");
        }
        if (password != null && password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (enabled == null) {
            throw new IllegalArgumentException("Enabled status is required");
        }
    }

    public static User create(String username, String password, String email, Set<Role> roles) {
        User user = User.builder()
                .username(username)
                .password(password)
                .email(email)
                .enabled(true)
                .roles(roles != null ? roles : Set.of())
                .build();
        user.validate();
        return user;
    }
}
