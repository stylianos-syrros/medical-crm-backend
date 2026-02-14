package com.medicalcrm.backend.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentResponse {

    private Long id;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
    private String doctorName;
    private String serviceName;
    private String notes;
}
