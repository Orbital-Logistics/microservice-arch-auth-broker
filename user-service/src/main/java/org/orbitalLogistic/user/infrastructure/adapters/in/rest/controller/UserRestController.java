package org.orbitalLogistic.user.infrastructure.adapters.in.rest.controller;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.user.application.ports.in.DeleteUserUseCase;
import org.orbitalLogistic.user.application.ports.in.GetUsersUseCase;
import org.orbitalLogistic.user.application.ports.in.UpdateUserUseCase;
import org.orbitalLogistic.user.domain.exception.UserNotFoundException;
import org.orbitalLogistic.user.domain.model.User;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.UpdateUserRequest;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.UserResponse;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.mapper.UserRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private final GetUsersUseCase getUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UserRestMapper userRestMapper;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        User user = getUsersUseCase.getById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + id + "' not found"));
        return ResponseEntity.ok(userRestMapper.toResponse(user));
    }

    @GetMapping("/{id}/username")
    public ResponseEntity<String> getUsernameById(@PathVariable Long id) {
        User user = getUsersUseCase.getById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + id + "' not found"));
        return ResponseEntity.ok(user.getUsername());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getByEmail(@PathVariable String email) {
        User user = getUsersUseCase.getByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email '" + email + "' not found"));
        return ResponseEntity.ok(userRestMapper.toResponse(user));
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> userExists(@PathVariable Long id) {
        boolean exists = getUsersUseCase.getById(id).isPresent();
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<?> getUserRoles(@PathVariable Long id) {
        User user = getUsersUseCase.getById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + id + "' not found"));
        return ResponseEntity.ok(user.getRoles());
    }

    @PutMapping("/update")
    public ResponseEntity<UserResponse> updateUser(@RequestBody UpdateUserRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = getUsersUseCase.getByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User with username '" + currentUsername + "' not found"));
        
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
        boolean isSelfUpdate = currentUser.getId().equals(request.id());
        
        if (!isAdmin && !isSelfUpdate) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        User user = updateUserUseCase.updateUser(userRestMapper.toCommand(request));
        return ResponseEntity.ok(userRestMapper.toResponse(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        deleteUserUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
