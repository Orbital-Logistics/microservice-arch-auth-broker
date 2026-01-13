package org.orbitalLogistic.user.application.ports.out;

import org.orbitalLogistic.user.domain.model.User;

public interface JwtTokenPort {
    String generateToken(User user);
    String extractUsername(String token);
    boolean validateToken(String token, User user);
}
