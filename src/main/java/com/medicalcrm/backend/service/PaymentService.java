package com.medicalcrm.backend.service;

import com.medicalcrm.backend.dto.request.CreatePaymentRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.dto.response.PaymentResponse;

import java.math.BigDecimal;
import java.util.List;


public interface PaymentService {

    // DOCTOR

    BigDecimal getTotalReceivedByDoctor(Long doctorId);
    BigDecimal getTotalPendingByDoctor(Long doctorId);
    BigDecimal getTotalExpectedByDoctor(Long doctorId);

    List<AppointmentResponse> getPaidAppointmentsByDoctor(Long doctorId);
    List<AppointmentResponse> getUnpaidAppointmentsByDoctor(Long doctorId);

    // PATIENT

    PaymentResponse makePayment(Long patientId, CreatePaymentRequest request);

    List<AppointmentResponse> getPaidAppointmentsByPatient(Long patientId);
    List<AppointmentResponse> getUnpaidAppointmentsByPatient(Long patientId);

    BigDecimal getTotalPaidByPatient(Long patientId);
    BigDecimal getTotalPendingByPatient(Long patientId);
    BigDecimal getTotalExpectedByPatient(Long patientId);

    // ADMIN

    List<PaymentResponse> getAllPayments();

}
