package com.medicalcrm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import com.medicalcrm.backend.model.DoctorSpecialty;

import lombok.Data;

@Data
public class CreateDoctorRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Specialty is required")
    private DoctorSpecialty specialty;

    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone must be 10 digits"
    )
    private String phone;
}
