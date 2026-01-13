package org.orbitalLogistic.user.application.ports.out;

import org.orbitalLogistic.user.domain.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Role save(Role role);
    Optional<Role> findById(Long id);
    Optional<Role> findByName(String name);
    List<Role> findAll();
    boolean existsById(Long id);
    boolean existsByName(String name);
    void deleteById(Long id);
}
