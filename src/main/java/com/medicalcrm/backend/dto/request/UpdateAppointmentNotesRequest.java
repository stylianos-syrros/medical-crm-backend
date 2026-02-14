package com.medicalcrm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAppointmentNotesRequest {

    @NotBlank(message = "Notes cannot be empty")
    private String notes;
}
