package org.orbitalLogistic.user.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.user.dto.request.RoleRequestDTO;
import org.orbitalLogistic.user.dto.response.RolesResponseDTO;
import org.orbitalLogistic.user.dto.response.UserResponseDTO;
import org.orbitalLogistic.user.services.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
@Validated
public class RoleController {

    private final RoleService roleService;

        @GetMapping("/get")
        public ResponseEntity<RolesResponseDTO> getAllRoles() {
            RolesResponseDTO responseDTO = RolesResponseDTO.builder()
                    .roles(roleService.getAllRolesStrings())
                    .build();

            return ResponseEntity
                    .ok()
                    .body(responseDTO);
        }
}
