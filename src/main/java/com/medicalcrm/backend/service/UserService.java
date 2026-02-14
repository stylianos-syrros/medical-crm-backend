package com.medicalcrm.backend.service;

import com.medicalcrm.backend.model.User;
import com.medicalcrm.backend.model.Role;

import javax.swing.*;
import java.util.List;

public interface UserService {

    User createUser(String username,
                    String password,
                    String email,
                    Role role
    );

    User getById(Long id);

    User getByUsername(String username);

    List<User> getAllUsers();

    void enableUser(Long userId);

    void disableUser(Long userId);

    void changePassword(Long userId,
                        String oldPassword,
                        String newPassword
    );

    void changeRole(Long usrId, Role role);

    void deleteUser(Long userId);

    void updateEmail(String username, String newEmail);

}

