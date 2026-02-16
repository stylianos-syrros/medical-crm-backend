package com.medicalcrm.backend.service.impl;

import com.medicalcrm.backend.dto.request.CreatePaymentRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.dto.response.PaymentResponse;
import com.medicalcrm.backend.exception.BusinessException;
import com.medicalcrm.backend.exception.NotFoundException;

import com.medicalcrm.backend.mapper.AppointmentMapper;
import com.medicalcrm.backend.mapper.PaymentMapper;
import com.medicalcrm.backend.model.*;

import com.medicalcrm.backend.repository.AppointmentRepository;
import com.medicalcrm.backend.repository.PatientRepository;
import com.medicalcrm.backend.repository.PaymentRepository;

import com.medicalcrm.backend.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j

public class PaymentServiceImpl implements PaymentService{

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    // DOCTOR

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalReceivedByDoctor(Long doctorId){

        BigDecimal received = paymentRepository.sumPaymentsToDoctor(doctorId);

        log.info("Doctor {} total received: {}", doctorId, received);

        return received;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpectedByDoctor(Long doctorId){

        BigDecimal total = appointmentRepository.sumAppointmentPricesToDoctor(doctorId);

        log.info("Doctor {} total expected: {}", doctorId, total);

        return total;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingByDoctor(Long doctorId){

        BigDecimal received = paymentRepository.sumPaymentsToDoctor(doctorId);
        BigDecimal total = appointmentRepository.sumAppointmentPricesToDoctor(doctorId);

        BigDecimal pending = total.subtract(received);

        if (pending.compareTo(BigDecimal.ZERO) < 0) {

            log.error(
                    "Negative pending detected for doctor {} (total={}, received={})",
                    doctorId, total, received
            );

            pending = BigDecimal.ZERO;
        }


        log.info("Doctor {} total pending: {}", doctorId, pending);

        return pending;
    }

    // PATIENT

    @Override
    public PaymentResponse makePayment(Long patientId, CreatePaymentRequest request) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patientId)) {
            throw new BusinessException("This appointment does not belong to patient");
        }

        // συνολικό ποσό που έχει πληρωθεί ήδη
        BigDecimal totalPaid = Optional.ofNullable(
                paymentRepository.sumAmountByAppointmentId(appointment.getId())
        ).orElse(BigDecimal.ZERO);

        BigDecimal price = appointment.getService().getPrice();

        BigDecimal remaining = price.subtract(totalPaid);

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Appointment already fully paid");
        }

        BigDecimal paymentAmount = request.getAmount();

        if (paymentAmount.compareTo(remaining) > 0) {
            throw new BusinessException("Payment exceeds remaining amount");
        }

        Payment payment = PaymentMapper.toEntity(request, appointment, patient);

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        log.info("Patient {} paid {} for appointment {}",
                patientId, paymentAmount, appointment.getId());

        return PaymentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPaidAppointmentsByPatient(Long patientId) {

        List<Appointment> appointments =
                appointmentRepository.findByPatientId(patientId);

        return appointments.stream()
                .filter(a -> {

                    BigDecimal paid = Optional.ofNullable(
                            paymentRepository.sumAmountByAppointmentId(a.getId())
                    ).orElse(BigDecimal.ZERO);

                    BigDecimal price = a.getService().getPrice();

                    return paid.compareTo(price) >= 0;
                })
                .map(AppointmentMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUnpaidAppointmentsByPatient(Long patientId) {

        List<Appointment> appointments =
                appointmentRepository.findByPatientId(patientId);

        return appointments.stream()
                .filter(a -> {

                    BigDecimal paid = Optional.ofNullable(
                            paymentRepository.sumAmountByAppointmentId(a.getId())
                    ).orElse(BigDecimal.ZERO);

                    BigDecimal price = a.getService().getPrice();

                    return paid.compareTo(price) < 0;
                })
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidByPatient(Long patientId) {

        return paymentRepository.sumPaymentsByPatient(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingByPatient(Long patientId) {

        BigDecimal paid = getTotalPaidByPatient(patientId);

        BigDecimal total = appointmentRepository.sumAppointmentPricesByPatient(patientId);

        BigDecimal pending = total.subtract(paid);

        if (pending.compareTo(BigDecimal.ZERO) < 0) {
            pending = BigDecimal.ZERO;
        }

        return pending;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpectedByPatient(Long patientId) {

        return appointmentRepository
                .sumAppointmentPricesByPatient(patientId);
    }

    // ADMIN

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {

        return paymentRepository.findAll()
                .stream()
                .map(PaymentMapper::toResponse)
                .toList();
    }
}
