package org.orbitalLogistic.user.repositories;

import org.jspecify.annotations.NonNull;
import org.orbitalLogistic.user.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findById(@NonNull Long id);
    Optional<User> findByEmail(String email);

    boolean existsById(@NonNull Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<User> getUserById(Long id);
}
