package com.medicalcrm.backend.dto.response;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;
    private String username;
    private String role;
}
