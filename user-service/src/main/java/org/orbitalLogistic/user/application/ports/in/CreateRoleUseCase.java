package org.orbitalLogistic.user.application.ports.in;

import org.orbitalLogistic.user.domain.model.Role;

public interface CreateRoleUseCase {
    Role createRole(CreateRoleCommand command);
}
