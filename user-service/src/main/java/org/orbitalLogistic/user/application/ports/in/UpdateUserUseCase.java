package org.orbitalLogistic.user.application.ports.in;

import org.orbitalLogistic.user.domain.model.User;

import java.util.Set;

public interface UpdateUserUseCase {
    User updateUser(UpdateUserCommand command);
    void changePassword(Long userId, String newPassword);
    User grantRoles(Long userId, Set<Long> roleIds);
    User revokeRoles(Long userId, Set<Long> roleIds);
}
