package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreateUserRequest;
import com.medicalcrm.backend.dto.request.UpdateUserRequest;
import com.medicalcrm.backend.dto.response.UserResponse;
import com.medicalcrm.backend.exception.BusinessException;
import com.medicalcrm.backend.exception.NotFoundException;
import com.medicalcrm.backend.model.Role;
import com.medicalcrm.backend.model.User;
import com.medicalcrm.backend.repository.UserRepository;
import com.medicalcrm.backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUser_shouldReturnUserResponse_whenValidRequest() {

        //mockAuthentication("admin", Role.ADMIN);

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("john");
        request.setEmail("john@test.com");
        request.setPassword("password123");
        request.setRole(Role.ADMIN);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("john");
        savedUser.setEmail("john@test.com");
        savedUser.setRole(Role.ADMIN);

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals("john", response.getUsername());
    }

    @Test
    void createUser_shouldThrowException_whenUsernameExists() {

        //mockAuthentication("admin", Role.ADMIN);

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("john");
        request.setEmail("john@test.com");

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> userService.createUser(request));
    }

    @Test
    void getById_shouldReturnUser_whenExists() {

        mockAuthentication("john", Role.ADMIN);

        User user = new User();
        user.setId(1L);
        user.setUsername("john");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponse response = userService.getById(1L);

        assertEquals("john", response.getUsername());
    }



    @Test
    void getById_shouldThrowException_whenNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.getById(1L));
    }

    @Test
    void updateUser_shouldUpdateFields() {

        mockAuthentication("john", Role.ADMIN);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("newName");
        request.setEmail("new@test.com");

        User user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("old@test.com");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponse response = userService.updateUser(1L, request);

        assertEquals("newName", response.getUsername());
        assertEquals("new@test.com", response.getEmail());
    }

    @Test
    void changePassword_shouldUpdatePassword_whenOldMatches() {

        mockAuthentication("john", Role.ADMIN);

        User user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setPassword("oldEncoded");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("old", "oldEncoded"))
                .thenReturn(true);

        when(passwordEncoder.encode("new"))
                .thenReturn("newEncoded");

        userService.changePassword(1L, "old", "new");

        assertEquals("newEncoded", user.getPassword());
    }


    @Test
    void changePassword_shouldThrowException_whenOldWrong() {

        mockAuthentication("john", Role.ADMIN);

        User user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setPassword("oldEncoded");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(false);

        assertThrows(BusinessException.class,
                () -> userService.changePassword(1L, "wrong", "new"));
    }

    @Test
    void enableUser_shouldSetEnabledTrue() {

        User user = new User();
        user.setId(1L);
        user.setEnabled(false);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.enableUser(1L);

        assertTrue(user.getEnabled());
    }

    @Test
    void deleteUser_shouldDelete_whenExists() {

        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    private void mockAuthentication(String username, Role role) {

        Authentication auth = mock(Authentication.class);

        lenient().when(auth.getName()).thenReturn(username);

        lenient().when(auth.getAuthorities())
                .thenReturn((Collection) List.of(
                        new SimpleGrantedAuthority("ROLE_" + role.name())
                ));

        SecurityContext context = mock(SecurityContext.class);

        lenient().when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }



}
