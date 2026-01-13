package org.orbitalLogistic.user.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.user.application.ports.out.RoleRepository;
import org.orbitalLogistic.user.domain.model.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJdbcRepository jdbcRepository;
    private final RolePersistenceMapper mapper;

    @Override
    public Role save(Role role) {
        RoleEntity entity = mapper.toEntity(role);
        RoleEntity savedEntity = jdbcRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Role> findById(Long id) {
        return jdbcRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return jdbcRepository.findByName(name)
                .map(mapper::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return StreamSupport.stream(jdbcRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return jdbcRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jdbcRepository.existsByName(name);
    }

    @Override
    public void deleteById(Long id) {
        jdbcRepository.deleteById(id);
    }
}
