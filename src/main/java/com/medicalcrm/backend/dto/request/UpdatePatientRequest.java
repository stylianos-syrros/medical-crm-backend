package com.medicalcrm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import jakarta.validation.constraints.Past;


@Data
public class UpdatePatientRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone must be 10 digits"
    )
    private String phone;


    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

}

