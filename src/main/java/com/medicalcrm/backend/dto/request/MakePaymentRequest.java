package com.medicalcrm.backend.dto.request;

import com.medicalcrm.backend.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MakePaymentRequest {

    @NotNull(message = "Appointment id is required")
    private Long appointmentId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Positive(message = "Amount must be positive")
    private Double amount;
}
