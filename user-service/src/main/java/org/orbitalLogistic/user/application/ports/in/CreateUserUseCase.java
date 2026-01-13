package org.orbitalLogistic.user.application.ports.in;

import org.orbitalLogistic.user.domain.model.User;

public interface CreateUserUseCase {
    User createUser(CreateUserCommand command);
}
