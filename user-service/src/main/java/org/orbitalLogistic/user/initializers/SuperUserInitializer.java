package org.orbitalLogistic.user.initializers;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.user.application.ports.in.CreateUserCommand;
import org.orbitalLogistic.user.application.ports.in.CreateUserUseCase;
import org.orbitalLogistic.user.application.ports.in.GetRolesUseCase;
import org.orbitalLogistic.user.application.ports.in.GetUsersUseCase;
import org.orbitalLogistic.user.domain.model.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SuperUserInitializer implements CommandLineRunner {

    private final CreateUserUseCase createUserUseCase;
    private final GetUsersUseCase getUsersUseCase;
    private final GetRolesUseCase getRolesUseCase;

    @Value("${security.root-user-name}")
    private String rootUserName;

    @Value("${security.root-user-password}")
    private String rootUserPassword;

    @Value("${security.root-user-email}")
    private String rootUserEmail;

    @Value("${security.root-user-role}")
    private String rootUserRole;

    @Override
    public void run(String... args) {
        if (getUsersUseCase.getByUsername(rootUserName).isEmpty()) {
            Optional<Role> rootRole = getRolesUseCase.getByName(rootUserRole);
            if (rootRole.isEmpty()) {
                throw new IllegalStateException("Wrong config `root-user-role` value: " + rootUserRole);
            }

            CreateUserCommand command = new CreateUserCommand(
                rootUserName,
                rootUserPassword,
                rootUserEmail,
                Set.of(rootRole.get().getId())
            );

            createUserUseCase.createUser(command);
        }
    }
}
