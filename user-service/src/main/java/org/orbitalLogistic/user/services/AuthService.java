package org.orbitalLogistic.user.services;

import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.user.entities.Role;
import org.orbitalLogistic.user.entities.User;
import org.orbitalLogistic.user.exceptions.auth.*;
import org.orbitalLogistic.user.exceptions.common.BadRequestException;
import org.orbitalLogistic.user.exceptions.common.UnknownUsernameException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private final UserService usersService;
    private final RoleService roleService;
    private final JwtService jwtService;

    @Transactional
    public String signUp(String username, String password, String email, Set<String> roles) {

        if (usersService.userExists(username)) {
            throw new UsernameAlreadyExistsException("");
        }

        if (usersService.emailExists(email)) {
            throw new EmailAlreadyExistsException("");
        }

        Set<Role> validatedRoles = roleService.validateRoles(roles);

        User user = User.builder()
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(validatedRoles)
                .enabled(true)
                .build();

        usersService.create(user);

        return jwtService.generateToken(user);
    }

    @Transactional
    public String logIn(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    username,
                    password
            ));
        } catch (AuthenticationException e) {
            throw new WrongCredentialsException("");
        }

        var user = usersService
                .userDetailsService()
                .loadUserByUsername(username);

        return jwtService.generateToken(user);
    }

    @Transactional
    public void grantRoles(String username, Set<String> roles) {
        Optional<User> user = usersService.findByUsername(username);

        if (user.isEmpty()) {
            throw new UnknownUsernameException(username);
        }

        if (roles.isEmpty()) {
            throw new BadRequestException("Request should contains roles!");
        }

        Set<Role> validatedRoles = roleService.validateRoles(roles);
        user.get().getRoles().addAll(validatedRoles);

        usersService.createOrUpdateUser(user.get());
    }

    @Transactional
    public void revokeRoles(String username, Set<String> roles) {
        Optional<User> user = usersService.findByUsername(username);

        if (user.isEmpty()) {
            throw new UnknownUsernameException(username);
        }

        if (!roles.isEmpty()) {
            Set<Role> validatedRoles = roleService.validateRoles(roles);
            user.get().getRoles().removeAll(validatedRoles);
        } else {
            throw new BadRequestException("Roles cannot be empty");
        }

        usersService.createOrUpdateUser(user.get());
    }

    @Transactional
    public String changePassword(String username, String oldPassword, String newPassword) {
        if (oldPassword.isEmpty()) {
            throw new BadRequestException("Old password cannot be empty");
        }

        if (newPassword.isEmpty()) {
            throw new BadRequestException("New password cannot be empty");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    username,
                    oldPassword
            ));
        } catch (AuthenticationException e) {
            throw new WrongCredentialsException("");
        }

        User userEntity = usersService.getByUsername(username);
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        usersService.createOrUpdateUser(userEntity);

        UserDetails userDetails = usersService
                .userDetailsService()
                .loadUserByUsername(username);

        return jwtService.generateToken(userDetails);
    }
}
