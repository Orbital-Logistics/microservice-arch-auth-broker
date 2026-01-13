package org.orbitalLogistic.user.infrastructure.adapters.in.rest.mapper;

import org.orbitalLogistic.user.application.ports.in.LoginCommand;
import org.orbitalLogistic.user.application.ports.in.RegisterCommand;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.domain.model.User;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.AuthResponse;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.LoginRequest;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.RegisterRequest;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AuthRestMapper {

    public LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(
                request.username(),
                request.password()
        );
    }

    public RegisterCommand toCommand(RegisterRequest request) {
        return new RegisterCommand(
                request.username(),
                request.password(),
                request.email(),
                request.roleIds()
        );
    }

    public AuthResponse toResponse(String token, User user) {
        return new AuthResponse(
                token,
                user.getUsername(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
    }
}
