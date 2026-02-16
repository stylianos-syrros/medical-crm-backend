package com.medicalcrm.backend.dto.request;

import com.medicalcrm.backend.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {

    @NotNull(message = "Appointment id is required")
    private Long appointmentId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
}
