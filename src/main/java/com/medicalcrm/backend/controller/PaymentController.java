package com.medicalcrm.backend.controller;

import com.medicalcrm.backend.dto.request.CreatePaymentRequest;
import com.medicalcrm.backend.dto.response.PaymentResponse;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("patient/me")
    public PaymentResponse makePayment(@Valid @RequestBody CreatePaymentRequest request){

        return paymentService.makePayment(request);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("patient/me/paid")
    public List<AppointmentResponse> getPaidAppointmentsByPatient(){

        return paymentService.getPaidAppointmentsByPatient();
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/me/unpaid")
    public List<AppointmentResponse> getUnpaidAppointmentsByPatient() {

        return paymentService.getUnpaidAppointmentsByPatient();
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/me/total-paid")
    public BigDecimal getTotalPaidByPatient() {

        return paymentService.getTotalPaidByPatient();
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/me/total-pending")
    public BigDecimal getTotalPendingByPatient() {

        return paymentService.getTotalPendingByPatient();
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/me/total-expected")
    public BigDecimal getTotalExpectedByPatient() {

        return paymentService.getTotalExpectedByPatient();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/me/received")
    public BigDecimal getTotalReceivedByDoctor() {

        return paymentService.getTotalReceivedByDoctor();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/me/pending")
    public BigDecimal getTotalPendingByDoctor() {

        return paymentService.getTotalPendingByDoctor();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/me/expected")
    public BigDecimal getTotalExpectedByDoctor() {

        return paymentService.getTotalExpectedByDoctor();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/me/paid")
    public List<AppointmentResponse> getPaidAppointmentsByDoctor(){

        return paymentService.getPaidAppointmentsByDoctor();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/me/unpaid")
    public List<AppointmentResponse> getUnpaidAppointmentsByDoctor(){

        return paymentService.getUnpaidAppointmentsByDoctor();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public List<PaymentResponse> getAllPayments(){

        return paymentService.getAllPayments();
    }




}
