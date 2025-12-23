package org.orbitalLogistic.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 64, message = "Username must be between 2 and 64 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Username can only contain letters, numbers, dots, underscores and hyphens")
    private String username;

    @Email(message = "Email must be valid")
    @Size(max = 30, message = "Email must not exceed 30 characters")
    @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Invalid email format")
    private String email;

    @Size(min = 2, max = 64, message = "New username must be between 2 and 64 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "new username can only contain letters, numbers, dots, underscores and hyphens")
    private String newUsername;
}
