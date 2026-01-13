package org.orbitalLogistic.user.infrastructure.adapters.in.rest.mapper;

import org.orbitalLogistic.user.application.ports.in.CreateUserCommand;
import org.orbitalLogistic.user.application.ports.in.UpdateUserCommand;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.domain.model.User;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.CreateUserRequest;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.RoleResponse;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.UpdateUserRequest;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.UserResponse;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserRestMapper {

    public CreateUserCommand toCommand(CreateUserRequest request) {
        return new CreateUserCommand(
                request.username(),
                request.password(),
                request.email(),
                request.roleIds()
        );
    }

    public UpdateUserCommand toCommand(UpdateUserRequest request) {
        return new UpdateUserCommand(
                request.id(),
                request.username(),
                request.email()
        );
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getEnabled(),
                rolesToResponse(user.getRoles())
        );
    }

    public Set<RoleResponse> rolesToResponse(Set<Role> roles) {
        return roles.stream()
                .map(role -> new RoleResponse(role.getId(), role.getName()))
                .collect(Collectors.toSet());
    }
}
