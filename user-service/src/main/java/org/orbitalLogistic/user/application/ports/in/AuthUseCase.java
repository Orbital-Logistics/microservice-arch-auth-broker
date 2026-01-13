package org.orbitalLogistic.user.application.ports.in;

public interface AuthUseCase {
    String login(LoginCommand command);
    String register(RegisterCommand command);
}
