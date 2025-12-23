package org.orbitalLogistic.user.controllers;

import jakarta.validation.Valid;
import lombok.Generated;
import lombok.RequiredArgsConstructor;

import org.orbitalLogistic.user.dto.response.UserResponseDTO;
import org.orbitalLogistic.user.entities.Role;
import org.orbitalLogistic.user.entities.User;
import org.orbitalLogistic.user.exceptions.common.BadRequestException;
import org.orbitalLogistic.user.exceptions.update.EmptyUpdateRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.orbitalLogistic.user.dto.request.UpdateUserRequestDTO;
import org.orbitalLogistic.user.services.UserService;
import org.springframework.validation.annotation.Validated;

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

        UserResponseDTO userResponseDTO = UserResponseDTO.builder()
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
                .body(userResponseDTO);
    }

    // MARK implement
//    @GetMapping("/username/{username}")
//    public ResponseEntity<> getUserByUsername(String username) {
//
//    }
//    @GetMapping("id/{id}")
//    public ResponseEntity<> getUserById(Long id) {
//
//    }
//    @GetMapping("email/{email}")
//    public ResponseEntity<> getUserByEmail(String email) {
//
//    }
//    @GetMapping("/{id}/roles")
//    public ResponseEntity<> getUserRoles(Long id) {
//
//    }
//    @PostMapping("create")
//    public ResponseEntity<> getUserRoles(Long id) {
//
//    }

//    @GetMapping("/{id}")
//    public Mono<ResponseEntity<UserResponseDTO>> getUserById(@PathVariable Long id) {
//        return userService.findUserById(id)
//                .map(response ->
//                        ResponseEntity
//                                .ok()
//                                .body(response));
//    }

//    @GetMapping("/{id}/username")
//    public Mono<ResponseEntity<String>> getUsernameById(@PathVariable Long id) {
//        return userService.findUserById(id)
//                .map(response ->
//                        ResponseEntity
//                                .ok()
//                                .body(response.username()));
//    }

//    @PutMapping("/{id}")
//    public Mono<ResponseEntity<UserResponseDTO>> updateUser(
//            @PathVariable Long id,
//            @Valid @RequestBody UpdateUserRequestDTO request
//    ) {
//        return userService.updateUser(id, request)
//                .map(response ->
//                        ResponseEntity
//                                .ok()
//                                .body(response));
//    }

//    @DeleteMapping("/{id}")
//    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable Long id) {
//        return userService.deleteUser(id).thenReturn(ResponseEntity.noContent().build());
//    }

//    @GetMapping("/{id}/exists")
//    public Mono<ResponseEntity<Boolean>> userExists(@PathVariable Long id) {
//        return userService.userExists(id)
//                .map(ResponseEntity::ok);
//    }
}
