package org.orbitalLogistic.user.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.user.entities.Role;
import org.orbitalLogistic.user.exceptions.auth.UnknownRoleException;
import org.orbitalLogistic.user.exceptions.roles.RoleAlreadyExistsException;
import org.orbitalLogistic.user.exceptions.roles.RoleDoesNotExistException;
import org.orbitalLogistic.user.repositories.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(rollbackFor = Exception.class)
    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createRole(String name) {

        Optional<Role> role = roleRepository.findByName(name);
        if (role.isPresent()) {
            throw new RoleAlreadyExistsException(name);
        }

        Role newRole = Role.builder()
                .name(name)
                .build();

        roleRepository.save(newRole);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeRole(String name) {

        Optional<Role> role = roleRepository.findByName(name);
        if (role.isEmpty()) {
            throw new RoleDoesNotExistException(name);
        }

        roleRepository.deleteRoleByName(name);
    }

    public Set<Role> validateRoles(Set<String> roleNames) {
        List<Role> activeRoles = roleRepository.findAll();
        Set<Role> validatedRoles = new HashSet<>();

        for (String roleName : roleNames) {
            boolean valid = false;
            for (Role activeRole : activeRoles) {
                if (activeRole.getName().equals(roleName)) {
                    validatedRoles.add(activeRole);
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                throw new UnknownRoleException(roleName);
            }
        }
        return validatedRoles;
    }

    public Set<String> rolesToString(Set<Role> roles) {
        List<Role> activeRoles = roleRepository.findAll();
        Set<String> validatedRoles = new HashSet<>();

        for (Role role : roles) {
            boolean valid = false;
            for (Role activeRole : activeRoles) {
                if (activeRole.getName().equals(role.getName())) {
                    validatedRoles.add(activeRole.getName());
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                throw new UnknownRoleException(role.getName());
            }
        }
        return validatedRoles;
    }
}
