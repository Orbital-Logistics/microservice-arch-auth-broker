package org.orbitalLogistic.user.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.orbitalLogistic.user.dto.response.RolesResponseDTO;
import org.orbitalLogistic.user.dto.response.UserResponseDTO;
import org.orbitalLogistic.user.entities.Role;
import org.orbitalLogistic.user.entities.User;
import org.orbitalLogistic.user.exceptions.common.BadRequestException;
import org.orbitalLogistic.user.exceptions.common.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.orbitalLogistic.user.dto.request.UpdateUserRequestDTO;
import org.orbitalLogistic.user.services.UserService;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<UserResponseDTO> updateUser(@Valid @RequestBody UpdateUserRequestDTO request) {
        if (request.getUsername() == null) {
            throw new BadRequestException("Username is required");
        }

        User user = userService.updateUser(request.getUsername(), request.getNewUsername(), request.getEmail());

        UserResponseDTO responseDTO = UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();

        return ResponseEntity
                .ok()
                .body(responseDTO);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.findUserByEmail(email);

        if (user.isEmpty()) {
            throw new NotFoundException("");
        }

        UserResponseDTO responseDTO = UserResponseDTO.builder()
                .id(user.get().getId())
                .username(user.get().getUsername())
                .email(user.get().getEmail())
                .roles(user.get().getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();

        return ResponseEntity
                .ok()
                .body(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findUserById(id);

        if (user.isEmpty()) {
            throw new NotFoundException("");
        }

        UserResponseDTO responseDTO = UserResponseDTO.builder()
                .id(user.get().getId())
                .username(user.get().getUsername())
                .email(user.get().getEmail())
                .roles(user.get().getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();

        return ResponseEntity
                .ok()
                .body(responseDTO);
    }

    @GetMapping("/{id}/username")
    public ResponseEntity<String> getUsernameById(@PathVariable Long id) {
        Optional<User> user = userService.findUserById(id);

        if (user.isEmpty()) {
            throw new NotFoundException("");
        }

        return ResponseEntity
                .ok()
                .body(user.get().getUsername());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> userExists(@PathVariable Long id) {
        return ResponseEntity.ok(userService.userExists(id));
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<RolesResponseDTO> getUserRoles(@PathVariable Long id) {
        Optional<User> user = userService.findUserById(id);

        if (user.isEmpty()) {
            throw new NotFoundException("");
        }

        RolesResponseDTO responseDTO = RolesResponseDTO.builder()
                .roles(user.get().getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();

        return ResponseEntity
                .ok()
                .body(responseDTO);
    }
}
