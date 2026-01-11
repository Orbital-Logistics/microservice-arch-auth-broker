package org.orbitalLogistic.user.services;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.orbitalLogistic.user.entities.User;
import org.orbitalLogistic.user.exceptions.common.BadRequestException;
import org.orbitalLogistic.user.exceptions.auth.UnknownUsernameException;
import org.orbitalLogistic.user.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    public User updateUser(String username, @Nullable String newUsername, @Nullable String email) {

        Optional<User> updatedUser = userRepository.findByUsername(username);

        if (updatedUser.isEmpty()) {
            throw new UnknownUsernameException(username);
        }

        if (newUsername != null) {
            if (!newUsername.isEmpty()) {
                updatedUser.get().setUsername(newUsername);
            } else {
                throw new BadRequestException("New username cannot be empty");
            }
        }

        if (email != null) {
            if (!email.isEmpty()) {
                updatedUser.get().setEmail(email);
            } else {
                throw new BadRequestException("Email cannot be empty");
            }
        }

        return userRepository.save(updatedUser.get());
    }

    public void createOrUpdateUser(User user) {
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        try {
            userRepository.deleteById(id);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new org.orbitalLogistic.user.exceptions.common.InvalidOperationException(
                "Cannot delete user with id: " + id + ". User is referenced by other entities."
            );
        }
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findUserByEmail(String email) {return userRepository.findByEmail(email);}

    public Optional<User> findUserById(Long id) {return userRepository.findById(id);}

    public Boolean userExists(Long id) {
        return userRepository.existsById(id);
    }

    public Boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public Boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User getByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty())   {
            throw new EntityNotFoundException("User not found!");
        }
        return user.get();
    }

    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }
}
