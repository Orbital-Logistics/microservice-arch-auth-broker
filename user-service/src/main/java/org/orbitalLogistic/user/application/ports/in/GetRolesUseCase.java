package org.orbitalLogistic.user.application.ports.in;

import org.orbitalLogistic.user.domain.model.Role;

import java.util.List;
import java.util.Optional;

public interface GetRolesUseCase {
    Optional<Role> getById(Long id);
    Optional<Role> getByName(String name);
    List<Role> getAll();
}
