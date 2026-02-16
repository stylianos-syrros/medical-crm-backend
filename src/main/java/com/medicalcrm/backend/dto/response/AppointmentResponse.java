package com.medicalcrm.backend.dto.response;

import com.medicalcrm.backend.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponse {

    private Long id;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private AppointmentStatus status;
    private String notes;

    private Long patientId;
    private Long doctorId;
    private Long serviceId;
}

