package com.medicalcrm.backend.dto.response;

import com.medicalcrm.backend.model.Role;
import java.time.LocalDateTime;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponse {

    private Long id;

    private String username;

    private String email;

    private Role role;

    private boolean enabled;

    private LocalDateTime createdAt;
}
