package com.medicalcrm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import lombok.Data;

@Data
public class CreateDoctorRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Specialty is required")
    private String specialty;

    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone must be 10 digits"
    )
    private String phone;

    @NotNull(message = "User id is required")
    private Long userId;
}
