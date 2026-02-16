package com.medicalcrm.backend.mapper;

import com.medicalcrm.backend.dto.request.CreateUserRequest;
import com.medicalcrm.backend.dto.request.UpdateUserRequest;
import com.medicalcrm.backend.dto.response.UserResponse;
import com.medicalcrm.backend.model.Role;
import com.medicalcrm.backend.model.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getEnabled(),
                user.getCreatedAt()
        );
    }

    public static User toEntity(CreateUserRequest request){
        User user = new User();

        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // encoding γίνεται στο service
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        return user;
    }

    public static void updateEntity(User user, UpdateUserRequest request) {

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setEnabled(request.isEnabled());
    }



}
