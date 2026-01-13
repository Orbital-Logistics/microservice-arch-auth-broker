package org.orbitalLogistic.user.infrastructure.adapters.in.rest.mapper;

import org.orbitalLogistic.user.application.ports.in.CreateRoleCommand;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.CreateRoleRequest;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.RoleResponse;
import org.springframework.stereotype.Component;

@Component
public class RoleRestMapper {

    public CreateRoleCommand toCommand(CreateRoleRequest request) {
        return new CreateRoleCommand(request.name());
    }

    public RoleResponse toResponse(Role role) {
        return new RoleResponse(role.getId(), role.getName());
    }
}
