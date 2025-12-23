package org.orbitalLogistic.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolesProcessRequestDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 64, message = "Username must be between 2 and 64 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Username can only contain letters, numbers, dots, underscores and hyphens")
    private String username;

    @NotEmpty(message = "Roles field is required")
    private Set<String> roles;
}
