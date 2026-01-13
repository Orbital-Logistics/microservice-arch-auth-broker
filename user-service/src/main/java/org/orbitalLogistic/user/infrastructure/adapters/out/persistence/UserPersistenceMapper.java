package org.orbitalLogistic.user.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserPersistenceMapper {

    private final RolePersistenceMapper rolePersistenceMapper;

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .password(entity.getPasswordHash())
                .email(entity.getEmail())
                .enabled(entity.getEnabled())
                .roles(rolesToDomain(entity.getRoles()))
                .build();
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        
        return UserEntity.builder()
                .id(domain.getId())
                .username(domain.getUsername())
                .passwordHash(domain.getPassword())
                .email(domain.getEmail())
                .enabled(domain.getEnabled())
                .roles(rolesToEntity(domain.getRoles()))
                .build();
    }

    public Set<Role> rolesToDomain(Set<RoleEntity> entities) {
        if (entities == null) {
            return Collections.emptySet();
        }
        
        return entities.stream()
                .map(rolePersistenceMapper::toDomain)
                .collect(Collectors.toSet());
    }

    public Set<RoleEntity> rolesToEntity(Set<Role> roles) {
        if (roles == null) {
            return Collections.emptySet();
        }
        
        return roles.stream()
                .map(rolePersistenceMapper::toEntity)
                .collect(Collectors.toSet());
    }
}
