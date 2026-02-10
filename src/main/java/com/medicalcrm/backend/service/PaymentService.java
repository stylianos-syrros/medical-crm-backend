package com.medicalcrm.backend.service;

import com.medicalcrm.backend.model.Payment;
import com.medicalcrm.backend.model.Appointment;
import com.medicalcrm.backend.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;


public interface PaymentService {

    // DOCTOR

    BigDecimal getTotalReceivedByDoctor(Long doctorId);

    BigDecimal getTotalPendingByDoctor(Long doctorId);

    BigDecimal getTotalExpectedByDoctor(Long doctorId);


    // PATIENT

    Payment makePayment(Long patientId,
                        Long appointmentId,
                        BigDecimal amount,
                        PaymentMethod method);

    List<Appointment> getPaidAppointmentsByPatient(Long patientId);

    List<Appointment> getUnpaidAppointmentsByPatient(Long patientId);

    BigDecimal getTotalPaidByPatient(Long patientId);

    BigDecimal getTotalPendingByPatient(Long patientId);

    BigDecimal getTotalExpectedByPatient(Long patientId);

    // ADMIN

    List<Payment> getAllPayments();

}
