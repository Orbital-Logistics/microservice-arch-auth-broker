package org.orbitalLogistic.user.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.user.dto.request.ChangePasswordRequestDTO;
import org.orbitalLogistic.user.dto.request.RolesProcessRequestDTO;
import org.orbitalLogistic.user.dto.request.SignInRequestDTO;
import org.orbitalLogistic.user.dto.request.SignUpRequestDTO;
import org.orbitalLogistic.user.dto.response.JwtAuthResponse;
import org.orbitalLogistic.user.dto.response.UserResponseDTO;
import org.orbitalLogistic.user.exceptions.update.EmptyUpdateRequestException;
import org.orbitalLogistic.user.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JwtAuthResponse> signUp(@Valid @RequestBody SignUpRequestDTO request) {
        String token = authService.signUp(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getRoles()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new JwtAuthResponse(token));
    }

    @PostMapping("/log-in")
    public ResponseEntity<JwtAuthResponse> logIn(@Valid @RequestBody SignInRequestDTO request) {
        String token = authService.logIn(
                request.getUsername(),
                request.getPassword()
        );
        return ResponseEntity
                .ok()
                .body(new JwtAuthResponse(token));
    }

    @PostMapping("/grant-roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> grantRole(@Valid @RequestBody RolesProcessRequestDTO request) {
        authService.grantRoles(request.getUsername(), request.getRoles());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/revoke-roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> revokeRoles(@Valid @RequestBody RolesProcessRequestDTO request) {
        if (request.getRoles() == null) {
            throw new EmptyUpdateRequestException("Request should contains roles");
        }

        authService.revokeRoles(request.getUsername(), request.getRoles());

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/change-password")
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<JwtAuthResponse> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request) {
        String token = authService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());

        return ResponseEntity
                .ok()
                .body(new JwtAuthResponse(token));
    }
}
