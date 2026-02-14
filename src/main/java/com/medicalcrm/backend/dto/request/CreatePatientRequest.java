package com.medicalcrm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreatePatientRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private String notes;

    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone must be 10 digits"
    )
    private String phone;

    @NotNull(message = "User id is required")
    private Long userId;
}
