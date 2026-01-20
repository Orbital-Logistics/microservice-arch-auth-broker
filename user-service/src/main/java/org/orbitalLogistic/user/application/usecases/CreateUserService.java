package org.orbitalLogistic.user.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.user.application.ports.in.CreateUserCommand;
import org.orbitalLogistic.user.application.ports.in.CreateUserUseCase;
import org.orbitalLogistic.user.application.ports.out.PasswordEncoderPort;
import org.orbitalLogistic.user.application.ports.out.ReportSender;
import org.orbitalLogistic.user.application.ports.out.RoleRepository;
import org.orbitalLogistic.user.application.ports.out.UserRepository;
import org.orbitalLogistic.user.domain.exception.EmailAlreadyExistsException;
import org.orbitalLogistic.user.domain.exception.RoleNotFoundException;
import org.orbitalLogistic.user.domain.exception.UserAlreadyExistsException;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.domain.model.User;
import org.orbitalLogistic.user.infrastructure.adapters.out.kafka.ReportPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateUserService implements CreateUserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final ReportSender reportSender;

    @Override
    @Transactional
    public User createUser(CreateUserCommand command) {
        log.info("Creating user with username: {}", command.username());

        // Проверяем существование username
        if (userRepository.existsByUsername(command.username())) {
            log.warn("Username already exists: {}", command.username());
            throw new UserAlreadyExistsException("User with username '" + command.username() + "' already exists");
        }

        // Проверяем существование email
        if (userRepository.existsByEmail(command.email())) {
            log.warn("Email already exists: {}", command.email());
            throw new EmailAlreadyExistsException("User with email '" + command.email() + "' already exists");
        }

        // Проверяем и загружаем роли
        Set<Role> roles = command.roleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> {
                            log.error("Role not found with id: {}", roleId);
                            return new RoleNotFoundException("Role with id '" + roleId + "' not found");
                        }))
                .collect(Collectors.toSet());

        // Шифруем пароль
        String encodedPassword = passwordEncoder.encode(command.password());

        // Создаём пользователя
        User user = User.create(command.username(), encodedPassword, command.email(), roles);

        // Сохраняем
        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());

        return savedUser;
    }
}
