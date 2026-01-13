package org.orbitalLogistic.user.infrastructure.adapters.in.rest.controller;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.user.application.ports.in.GetRolesUseCase;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.RoleResponse;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.mapper.RoleRestMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleRestController {

    private final GetRolesUseCase getRolesUseCase;
    private final RoleRestMapper roleRestMapper;

    @GetMapping("/get")
    public ResponseEntity<List<RoleResponse>> getAll() {
        List<Role> roles = getRolesUseCase.getAll();
        List<RoleResponse> response = roles.stream()
                .map(roleRestMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
