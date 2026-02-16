package com.medicalcrm.backend.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpdateAppointmentScheduleRequest {

    @NotNull(message = "Date is required")
    @Future(message = "Date must be in the future")
    private LocalDate appointmentDate;

    @NotNull(message = "Time is required")
    private LocalTime appointmentTime;
}
