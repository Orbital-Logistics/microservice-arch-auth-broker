package org.orbitalLogistic.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDTO {

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 30, message = "Email must not exceed 30 characters")
        @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Invalid email format")
        private String email;

        @NotBlank(message = "Username is required")
        @Size(min = 2, max = 64, message = "Username must be between 2 and 64 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Username can only contain letters, numbers, dots, underscores and hyphens")
        private String username;

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 15, message = "Password must be from 8 to 15 characters")
        private String password;

        @NotEmpty(message = "User roles cannot be empty")
        private Set<String> roles;
}
