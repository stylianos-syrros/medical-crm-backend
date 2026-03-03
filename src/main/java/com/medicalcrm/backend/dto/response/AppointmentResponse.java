package com.medicalcrm.backend.dto.response;

import com.medicalcrm.backend.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private String patientNotes;
    private String doctorNotes;

    private Long patientId;
    private Long doctorId;
    private Long serviceId;

    private String patientName;
    private String doctorName;
    private String serviceName;
    private BigDecimal totalPaid;
    private BigDecimal pendingAmount;

}

