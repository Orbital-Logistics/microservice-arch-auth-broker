package org.orbitalLogistic.user.infrastructure.adapters.out.persistence;

import org.orbitalLogistic.user.domain.model.Role;
import org.springframework.stereotype.Component;

@Component
public class RolePersistenceMapper {

    public Role toDomain(RoleEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Role.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public RoleEntity toEntity(Role domain) {
        if (domain == null) {
            return null;
        }
        
        return RoleEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .build();
    }
}
