package com.medicalcrm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;


import lombok.Data;

@Data
public class UpdateEmailRequest {

    @NotBlank
    @Email(message = "Invalid email format")
    private String email;
}
