package com.medicalcrm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.validation.constraints.Pattern;

@Data
public class UpdateDoctorRequest {

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
}

