package org.orbitalLogistic.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequestDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 64, message = "Username must be between 2 and 64 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Username can only contain letters, numbers, dots, underscores and hyphens")
    private String username;

    @NotBlank(message = "Old password is required!")
    @Size(min = 8, max = 15, message = "Old password must be from 8 to 15 characters")
    private String oldPassword;

    @NotBlank(message = "New password is required!")
    @Size(min = 8, max = 15, message = "New password must be from 8 to 15 characters")
    private String newPassword;
}
