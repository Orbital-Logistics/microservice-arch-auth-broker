package org.orbitalLogistic.user.initializers;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.user.entities.Role;
import org.orbitalLogistic.user.entities.User;
import org.orbitalLogistic.user.exceptions.initialize.RootUserInitException;
import org.orbitalLogistic.user.services.RoleService;
import org.orbitalLogistic.user.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SuperUserInitializer implements CommandLineRunner {

    private final UserService userService;
    private final RoleService roleService;

    private final PasswordEncoder passwordEncoder;

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
        if (!userService.userExists(rootUserName)) {
            Optional<Role> rootRole = roleService.getRoleByName(rootUserRole);
            if (rootRole.isEmpty()) {
                throw new RootUserInitException("Wrong config `root-user-role` value!");
            }

            User admin = User.builder()
                .username(rootUserName)
                .password(passwordEncoder.encode(rootUserPassword))
                .email(rootUserEmail)
                .enabled(true)
                .roles(Set.of(rootRole.get()))
                .build();

            userService.createOrUpdateUser(admin);
        }
    }
}
