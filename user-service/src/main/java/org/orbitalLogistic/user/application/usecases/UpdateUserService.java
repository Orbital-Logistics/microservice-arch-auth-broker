package org.orbitalLogistic.user.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.user.application.ports.in.UpdateUserCommand;
import org.orbitalLogistic.user.application.ports.in.UpdateUserUseCase;
import org.orbitalLogistic.user.application.ports.out.PasswordEncoderPort;
import org.orbitalLogistic.user.application.ports.out.RoleRepository;
import org.orbitalLogistic.user.application.ports.out.UserRepository;
import org.orbitalLogistic.user.domain.exception.EmailAlreadyExistsException;
import org.orbitalLogistic.user.domain.exception.RoleNotFoundException;
import org.orbitalLogistic.user.domain.exception.UserAlreadyExistsException;
import org.orbitalLogistic.user.domain.exception.UserNotFoundException;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    @Override
    @Transactional
    public User updateUser(UpdateUserCommand command) {
        log.info("Updating user with id: {}", command.id());

        // Загружаем пользователя
        User existingUser = userRepository.findById(command.id())
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", command.id());
                    return new UserNotFoundException("User with id '" + command.id() + "' not found");
                });

        // Проверяем уникальность нового username, если он изменился
        if (!existingUser.getUsername().equals(command.username())) {
            if (userRepository.existsByUsername(command.username())) {
                log.warn("Username already exists: {}", command.username());
                throw new UserAlreadyExistsException("User with username '" + command.username() + "' already exists");
            }
        }

        // Проверяем уникальность нового email, если он изменился
        if (!existingUser.getEmail().equals(command.email())) {
            if (userRepository.existsByEmail(command.email())) {
                log.warn("Email already exists: {}", command.email());
                throw new EmailAlreadyExistsException("User with email '" + command.email() + "' already exists");
            }
        }

        // Обновляем поля через toBuilder
        User updatedUser = existingUser.toBuilder()
                .username(command.username())
                .email(command.email())
                .build();

        // Валидируем обновлённого пользователя
        updatedUser.validate();

        // Сохраняем
        User savedUser = userRepository.save(updatedUser);
        log.info("User updated successfully with id: {}", savedUser.getId());

        return savedUser;
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        log.info("Changing password for user with id: {}", userId);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new UserNotFoundException("User with id '" + userId + "' not found");
                });

        String encodedPassword = passwordEncoderPort.encode(newPassword);
        User updatedUser = existingUser.toBuilder()
                .password(encodedPassword)
                .build();

        userRepository.save(updatedUser);
        log.info("Password changed successfully for user with id: {}", userId);
    }

    @Override
    @Transactional
    public User grantRoles(Long userId, Set<Long> roleIds) {
        log.info("Granting roles {} to user with id: {}", roleIds, userId);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new UserNotFoundException("User with id '" + userId + "' not found");
                });

        Set<Role> rolesToGrant = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> {
                            log.error("Role not found with id: {}", roleId);
                            return new RoleNotFoundException("Role with id '" + roleId + "' not found");
                        }))
                .collect(Collectors.toSet());

        Set<Role> updatedRoles = new HashSet<>(existingUser.getRoles());
        updatedRoles.addAll(rolesToGrant);

        User updatedUser = existingUser.toBuilder()
                .roles(updatedRoles)
                .build();

        User savedUser = userRepository.save(updatedUser);
        log.info("Roles granted successfully to user with id: {}", userId);

        return savedUser;
    }

    @Override
    @Transactional
    public User revokeRoles(Long userId, Set<Long> roleIds) {
        log.info("Revoking roles {} from user with id: {}", roleIds, userId);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new UserNotFoundException("User with id '" + userId + "' not found");
                });

        Set<Role> updatedRoles = existingUser.getRoles().stream()
                .filter(role -> !roleIds.contains(role.getId()))
                .collect(Collectors.toSet());

        User updatedUser = existingUser.toBuilder()
                .roles(updatedRoles)
                .build();

        User savedUser = userRepository.save(updatedUser);
        log.info("Roles revoked successfully from user with id: {}", userId);

        return savedUser;
    }
}
