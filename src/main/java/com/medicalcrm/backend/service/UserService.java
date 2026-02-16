package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreateUserRequest;
import com.medicalcrm.backend.dto.request.UpdateUserRequest;
import com.medicalcrm.backend.dto.response.UserResponse;
import com.medicalcrm.backend.model.User;
import com.medicalcrm.backend.model.Role;

import javax.swing.*;
import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getById(Long id);

    UserResponse updateUser(Long userId, UpdateUserRequest request);

    User getByUsername(String username);

    List<UserResponse> getAllUsers();

    void enableUser(Long userId);

    void disableUser(Long userId);

    void changePassword(Long userId,
                        String oldPassword,
                        String newPassword
    );

    void changeRole(Long usrId, Role role);

    void deleteUser(Long userId);
}

