package org.orbitalLogistic.user.infrastructure.adapters.in.rest.controller;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.user.application.ports.in.AuthUseCase;
import org.orbitalLogistic.user.application.ports.in.GetUsersUseCase;
import org.orbitalLogistic.user.application.ports.in.UpdateUserUseCase;
import org.orbitalLogistic.user.domain.exception.UserNotFoundException;
import org.orbitalLogistic.user.domain.model.User;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.AuthResponse;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.ChangePasswordRequest;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.LoginRequest;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.ManageRolesRequest;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.RegisterRequest;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.UserResponse;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.mapper.AuthRestMapper;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.mapper.UserRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthUseCase authUseCase;
    private final GetUsersUseCase getUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final AuthRestMapper authRestMapper;
    private final UserRestMapper userRestMapper;

    @PostMapping("/log-in")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        String token = authUseCase.login(authRestMapper.toCommand(request));
        User user = getUsersUseCase.getByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(authRestMapper.toResponse(token, user));
    }

    @PostMapping("/sign-up")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        String token = authUseCase.register(authRestMapper.toCommand(request));
        User user = getUsersUseCase.getByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authRestMapper.toResponse(token, user));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = getUsersUseCase.getByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User with username '" + currentUsername + "' not found"));
        
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
        boolean isSelfUpdate = currentUser.getId().equals(request.userId());
        
        if (!isAdmin && !isSelfUpdate) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        updateUserUseCase.changePassword(request.userId(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/revoke-roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> revokeRoles(@RequestBody ManageRolesRequest request) {
        User user = updateUserUseCase.revokeRoles(request.userId(), request.roleIds());
        return ResponseEntity.ok(userRestMapper.toResponse(user));
    }

    @PostMapping("/grant-roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> grantRoles(@RequestBody ManageRolesRequest request) {
        User user = updateUserUseCase.grantRoles(request.userId(), request.roleIds());
        return ResponseEntity.ok(userRestMapper.toResponse(user));
    }
}
