package org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DTO Tests")
class DtoTest {

    @Test
    @DisplayName("Should create LoginRequest")
    void shouldCreateLoginRequest() {
        // Given & When
        LoginRequest request = new LoginRequest("username", "password");

        // Then
        assertEquals("username", request.username());
        assertEquals("password", request.password());
    }

    @Test
    @DisplayName("Should create RegisterRequest")
    void shouldCreateRegisterRequest() {
        // Given & When
        RegisterRequest request = new RegisterRequest("username", "password", "email@example.com", Set.of(1L));

        // Then
        assertEquals("username", request.username());
        assertEquals("password", request.password());
        assertEquals("email@example.com", request.email());
        assertEquals(1, request.roleIds().size());
    }

    @Test
    @DisplayName("Should create UpdateUserRequest")
    void shouldCreateUpdateUserRequest() {
        // Given & When
        UpdateUserRequest request = new UpdateUserRequest(1L, "newusername", "new@example.com");

        // Then
        assertEquals(1L, request.id());
        assertEquals("newusername", request.username());
        assertEquals("new@example.com", request.email());
    }

    @Test
    @DisplayName("Should create ChangePasswordRequest")
    void shouldCreateChangePasswordRequest() {
        // Given & When
        ChangePasswordRequest request = new ChangePasswordRequest(1L, "newpassword");

        // Then
        assertEquals(1L, request.userId());
        assertEquals("newpassword", request.newPassword());
    }

    @Test
    @DisplayName("Should create ManageRolesRequest")
    void shouldCreateManageRolesRequest() {
        // Given & When
        ManageRolesRequest request = new ManageRolesRequest(1L, Set.of(1L, 2L));

        // Then
        assertEquals(1L, request.userId());
        assertEquals(2, request.roleIds().size());
    }

    @Test
    @DisplayName("Should test record equality")
    void shouldTestRecordEquality() {
        // Given
        LoginRequest request1 = new LoginRequest("username", "password");
        LoginRequest request2 = new LoginRequest("username", "password");
        LoginRequest request3 = new LoginRequest("other", "password");

        // Then
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
    }
}
