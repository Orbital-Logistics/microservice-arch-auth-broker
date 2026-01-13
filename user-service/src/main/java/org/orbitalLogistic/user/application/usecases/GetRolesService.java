package org.orbitalLogistic.user.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.user.application.ports.in.GetRolesUseCase;
import org.orbitalLogistic.user.application.ports.out.RoleRepository;
import org.orbitalLogistic.user.domain.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetRolesService implements GetRolesUseCase {

    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> getById(Long id) {
        log.debug("Finding role by id: {}", id);
        return roleRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> getByName(String name) {
        log.debug("Finding role by name: {}", name);
        return roleRepository.findByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAll() {
        log.debug("Finding all roles");
        return roleRepository.findAll();
    }
}
