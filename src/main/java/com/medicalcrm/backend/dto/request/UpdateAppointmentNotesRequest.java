package com.medicalcrm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAppointmentNotesRequest {
    private String notes;
}
