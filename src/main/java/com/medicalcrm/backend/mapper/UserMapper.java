package com.medicalcrm.backend.mapper;

import com.medicalcrm.backend.dto.request.CreateUserRequest;
import com.medicalcrm.backend.dto.request.UpdateUserRequest;
import com.medicalcrm.backend.dto.response.UserResponse;
import com.medicalcrm.backend.model.Role;
import com.medicalcrm.backend.model.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setEnabled(user.getEnabled());
        response.setCreatedAt(user.getCreatedAt());
        response.setHasDoctorProfile(false);
        response.setHasPatientProfile(false);
        response.setDoctorProfileId(null);
        response.setPatientProfileId(null);
        return response;
    }

    public static User toEntity(CreateUserRequest request){
        User user = new User();

        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); 
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        return user;
    }

    public static void updateEntity(User user, UpdateUserRequest request) {

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

    }



}
