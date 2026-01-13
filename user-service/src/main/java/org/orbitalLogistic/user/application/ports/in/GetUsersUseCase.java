package org.orbitalLogistic.user.application.ports.in;

import org.orbitalLogistic.user.domain.model.User;

import java.util.Optional;

public interface GetUsersUseCase {
    Optional<User> getById(Long id);
    Optional<User> getByUsername(String username);
    Optional<User> getByEmail(String email);
    boolean existsById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
