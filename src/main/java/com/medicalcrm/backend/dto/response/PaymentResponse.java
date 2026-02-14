package com.medicalcrm.backend.dto.response;

import com.medicalcrm.backend.model.PaymentMethod;
import com.medicalcrm.backend.model.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {

    private Long id;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private LocalDateTime paidAt;
    private Long appointmentId;
    private Long patientId;
    private String patientEmail;
}
