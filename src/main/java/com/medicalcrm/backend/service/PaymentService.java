package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreatePaymentRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.dto.response.PaymentResponse;

import java.math.BigDecimal;
import java.util.List;


public interface PaymentService {

    // DOCTOR
    BigDecimal getTotalReceivedByDoctor();
    BigDecimal getTotalPendingByDoctor();
    BigDecimal getTotalExpectedByDoctor();

    List<AppointmentResponse> getPaidAppointmentsByDoctor();
    List<AppointmentResponse> getUnpaidAppointmentsByDoctor();

    // PATIENT
    PaymentResponse makePayment(CreatePaymentRequest request);

    List<AppointmentResponse> getPaidAppointmentsByPatient();
    List<AppointmentResponse> getUnpaidAppointmentsByPatient();

    BigDecimal getTotalPaidByPatient();
    BigDecimal getTotalPendingByPatient();
    BigDecimal getTotalExpectedByPatient();

    // ADMIN
    List<PaymentResponse> getAllPayments();

}
