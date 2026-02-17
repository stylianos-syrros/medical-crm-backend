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

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @PostMapping("patient/{patientId}")
    public PaymentResponse makePayment(
            @PathVariable Long patientId,
             @Valid @RequestBody CreatePaymentRequest request){

        return paymentService.makePayment(patientId, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @GetMapping("patient/{patientId}/paid")
    public List<AppointmentResponse> getPaidAppointmentsByPatient(
            @PathVariable Long patientId){

        return paymentService.getPaidAppointmentsByPatient(patientId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @GetMapping("/patient/{patientId}/unpaid")
    public List<AppointmentResponse> getUnpaidAppointmentsByPatient(
            @PathVariable Long patientId) {

        return paymentService.getUnpaidAppointmentsByPatient(patientId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @GetMapping("/patient/{patientId}/total-paid")
    public BigDecimal getTotalPaidByPatient(
            @PathVariable Long patientId) {

        return paymentService.getTotalPaidByPatient(patientId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @GetMapping("/patient/{patientId}/total-pending")
    public BigDecimal getTotalPendingByPatient(
            @PathVariable Long patientId) {

        return paymentService.getTotalPendingByPatient(patientId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @GetMapping("/patient/{patientId}/total-expected")
    public BigDecimal getTotalExpectedByPatient(
            @PathVariable Long patientId) {

        return paymentService.getTotalExpectedByPatient(patientId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/doctor/{doctorId}/received")
    public BigDecimal getTotalReceivedByDoctor(
            @PathVariable Long doctorId) {

        return paymentService.getTotalReceivedByDoctor(doctorId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/doctor/{doctorId}/pending")
    public BigDecimal getTotalPendingByDoctor(
            @PathVariable Long doctorId) {

        return paymentService.getTotalPendingByDoctor(doctorId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/doctor/{doctorId}/expected")
    public BigDecimal getTotalExpectedByDoctor(
            @PathVariable Long doctorId) {

        return paymentService.getTotalExpectedByDoctor(doctorId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/doctor/{doctorId}/paid")
    public List<AppointmentResponse> getPaidAppointmentsByDoctor(
            @PathVariable Long doctorId){

        return paymentService.getPaidAppointmentsByDoctor(doctorId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/doctor/{doctorId}/unpaid")
    public List<AppointmentResponse> getUnpaidAppointmentsByDoctor(
            @PathVariable Long doctorId){

        return paymentService.getUnpaidAppointmentsByDoctor(doctorId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public List<PaymentResponse> getAllPayments(){

        return paymentService.getAllPayments();
    }




}
