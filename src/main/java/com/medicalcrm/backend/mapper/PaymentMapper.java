package com.medicalcrm.backend.mapper;

import com.medicalcrm.backend.dto.request.CreatePaymentRequest;
import com.medicalcrm.backend.dto.response.PaymentResponse;
import com.medicalcrm.backend.model.*;

import java.time.LocalDateTime;

public class PaymentMapper {

    public static Payment toEntity(CreatePaymentRequest request,
                                        Appointment appointment,
                                        Patient patient) {

        Payment payment = new Payment();

        payment.setAmount(request.getAmount());
        payment.setMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());

        payment.setAppointment(appointment);
        payment.setPaidBy(patient);

        return payment;
    }

    public static PaymentResponse toResponse(Payment payment) {

        PaymentResponse response = new PaymentResponse();

        response.setId(payment.getId());
        response.setAmount(payment.getAmount());
        response.setMethod(payment.getMethod());
        response.setStatus(payment.getStatus());
        response.setPaidAt(payment.getPaidAt());

        response.setAppointmentId(payment.getAppointment().getId());
        response.setPatientId(payment.getPaidBy().getId());
        response.setPatientEmail(payment.getPaidBy().getUser().getEmail());

        return response;
    }
}
