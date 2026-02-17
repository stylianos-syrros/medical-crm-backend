package com.medicalcrm.backend.controller;

import com.medicalcrm.backend.dto.request.ChangeRoleRequest;
import com.medicalcrm.backend.dto.request.ChangePasswordRequest;
import com.medicalcrm.backend.dto.request.CreateUserRequest;
import com.medicalcrm.backend.dto.request.UpdateUserRequest;
import com.medicalcrm.backend.dto.response.UserResponse;
import com.medicalcrm.backend.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public UserResponse createUser(
            @Valid @RequestBody CreateUserRequest request) {

        return userService.createUser(request);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id){

        return userService.getById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping
    public List<UserResponse> getAllUsers(){

        return userService.getAllUsers();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        return userService.updateUser(id,request);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deletUser(@PathVariable Long id){

        userService.deleteUser(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}/enable")
    public void enableUser(@PathVariable Long id){

        userService.enableUser(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}/disable")
    public void disableUser(@PathVariable Long id){

        userService.disableUser(id);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/password")
    public void changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request){

        userService.changePassword(
                id,
                request.getOldPassword(),
                request.getNewPassword()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role")
    public void changeRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequest request){

        userService.changeRole(id, request.getRole());
    }

}
