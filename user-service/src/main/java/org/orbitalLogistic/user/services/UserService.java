package org.orbitalLogistic.user.services;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.orbitalLogistic.user.entities.Role;
import org.orbitalLogistic.user.entities.User;
import org.orbitalLogistic.user.exceptions.auth.ForbiddenException;
import org.orbitalLogistic.user.exceptions.auth.UsernameAlreadyExistsException;
import org.orbitalLogistic.user.exceptions.common.BadRequestException;
import org.orbitalLogistic.user.exceptions.common.UnknownUsernameException;
import org.orbitalLogistic.user.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;

    @Transactional(rollbackFor = Exception.class)
    public void create(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("");
        }
        userRepository.save(user);
    }

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

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

//    protected UserResponseDTO registerUser(SignUpRequestDTO request) {
//        if (userRepository.existsByEmail(request.email())) {
//            throw new UserAlreadyExistsException("User with email already exists");
//        }
//
//        if (userRepository.existsByUsername(request.username())) {
//            throw new UserAlreadyExistsException("User with such username already exists");
//        }
//
//        User user = userMapper.toEntity(request);
//        user.setPasswordHash(request.password());
//        UserRole userRole = roleRepository.findByName("logistics_officer")
//                .orElseThrow(() -> new DataNotFoundException("logistics_officer role not found"));
//        user.setRoleId(userRole.getId());
//
//        user = userRepository.save(user);
//        return toResponseDTO(user);
//    }

//    protected PageResponseDTO<UserResponseDTO> getUsers(String email, String username, int page, int size) {
//        int offset = page * size;
//        List<User> users = userRepository.findUsersWithFilters(email, username, size, offset);
//        long total = userRepository.countUsersWithFilters(email, username);
//
//        List<UserResponseDTO> userDTOs = users.stream().map(this::toResponseDTO).toList();
//
//        int totalPages = (int) Math.ceil((double) total / size);
//        return new PageResponseDTO<>(userDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
//    }

//    protected UserResponseDTO findUserById(Long id) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
//        return toResponseDTO(user);
//    }

//    protected UserResponseDTO findUserByEmail(String email) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UserNotFoundException("User not found"));
//        return toResponseDTO(user);
//    }

//    protected UserResponseDTO updateUser(Long id, UpdateUserRequestDTO request) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new UserNotFoundException("User not found"));
//
//        if (request.username() != null) user.setUsername(request.username());
//
//        user = userRepository.save(user);
//        return toResponseDTO(user);
//    }

    public User getEntityByIdOrNull(Long id) {
        return userRepository.findById(id).orElse(null);
    }

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
