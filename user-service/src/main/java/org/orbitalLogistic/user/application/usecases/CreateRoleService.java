package org.orbitalLogistic.user.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.user.application.ports.in.CreateRoleCommand;
import org.orbitalLogistic.user.application.ports.in.CreateRoleUseCase;
import org.orbitalLogistic.user.application.ports.out.RoleRepository;
import org.orbitalLogistic.user.domain.exception.RoleAlreadyExistsException;
import org.orbitalLogistic.user.domain.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateRoleService implements CreateRoleUseCase {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public Role createRole(CreateRoleCommand command) {
        log.info("Creating role with name: {}", command.name());

        // Проверяем существование роли по name
        if (roleRepository.existsByName(command.name().toUpperCase())) {
            log.warn("Role already exists with name: {}", command.name());
            throw new RoleAlreadyExistsException("Role with name '" + command.name() + "' already exists");
        }

        // Создаём роль
        Role role = Role.create(command.name());

        // Сохраняем
        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully with id: {}", savedRole.getId());

        return savedRole;
    }
}
