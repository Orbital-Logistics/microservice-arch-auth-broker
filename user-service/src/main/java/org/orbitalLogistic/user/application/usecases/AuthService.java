package org.orbitalLogistic.user.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.user.application.ports.in.AuthUseCase;
import org.orbitalLogistic.user.application.ports.in.LoginCommand;
import org.orbitalLogistic.user.application.ports.in.RegisterCommand;
import org.orbitalLogistic.user.application.ports.out.JwtTokenPort;
import org.orbitalLogistic.user.application.ports.out.PasswordEncoderPort;
import org.orbitalLogistic.user.application.ports.out.RoleRepository;
import org.orbitalLogistic.user.application.ports.out.UserRepository;
import org.orbitalLogistic.user.domain.exception.EmailAlreadyExistsException;
import org.orbitalLogistic.user.domain.exception.InvalidCredentialsException;
import org.orbitalLogistic.user.domain.exception.RoleNotFoundException;
import org.orbitalLogistic.user.domain.exception.UserAlreadyExistsException;
import org.orbitalLogistic.user.domain.exception.UserNotFoundException;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements AuthUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final JwtTokenPort jwtTokenPort;

    @Override
    @Transactional(readOnly = true)
    public String login(LoginCommand command) {
        log.info("Login attempt for username: {}", command.username());

        // Находим пользователя по username
        User user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", command.username());
                    return new UserNotFoundException("User with username '" + command.username() + "' not found");
                });

        // Проверяем пароль
        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            log.warn("Invalid password for username: {}", command.username());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Генерируем JWT токен
        String token = jwtTokenPort.generateToken(user);
        log.info("Login successful for username: {}", command.username());

        return token;
    }

    @Override
    @Transactional
    public String register(RegisterCommand command) {
        log.info("Registration attempt for username: {}", command.username());

        // Проверяем уникальность username
        if (userRepository.existsByUsername(command.username())) {
            log.warn("Username already exists: {}", command.username());
            throw new UserAlreadyExistsException("User with username '" + command.username() + "' already exists");
        }

        // Проверяем уникальность email
        if (userRepository.existsByEmail(command.email())) {
            log.warn("Email already exists: {}", command.email());
            throw new EmailAlreadyExistsException("User with email '" + command.email() + "' already exists");
        }

        // Проверяем, что роли указаны
        if (command.roleIds() == null || command.roleIds().isEmpty()) {
            log.error("Role IDs not provided for registration");
            throw new IllegalArgumentException("Role IDs must be provided");
        }

        // Получаем роли из команды
        Set<Role> roles = command.roleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> {
                            log.error("Role not found with id: {}", roleId);
                            return new RoleNotFoundException("Role with id '" + roleId + "' not found");
                        }))
                .collect(java.util.stream.Collectors.toSet());

        // Шифруем пароль
        String encodedPassword = passwordEncoder.encode(command.password());

        // Создаём пользователя с указанными ролями
        User user = User.create(command.username(), encodedPassword, command.email(), roles);

        // Сохраняем пользователя
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {} and roles: {}", savedUser.getId(), 
                roles.stream().map(Role::getName).collect(java.util.stream.Collectors.joining(", ")));

        // Генерируем JWT токен
        String token = jwtTokenPort.generateToken(savedUser);

        return token;
    }
}
