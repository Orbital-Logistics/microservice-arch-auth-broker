package org.orbitalLogistic.user.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.user.application.ports.in.DeleteUserUseCase;
import org.orbitalLogistic.user.application.ports.out.UserRepository;
import org.orbitalLogistic.user.domain.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteUserService implements DeleteUserUseCase {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        // Проверяем существование пользователя
        if (!userRepository.existsById(id)) {
            log.error("User not found with id: {}", id);
            throw new UserNotFoundException("User with id '" + id + "' not found");
        }

        // Удаляем пользователя
        userRepository.deleteById(id);
        log.info("User deleted successfully with id: {}", id);
    }
}
