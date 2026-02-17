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
import com.medicalcrm.backend.repository.DoctorRepository;
import com.medicalcrm.backend.repository.PatientRepository;
import com.medicalcrm.backend.repository.PaymentRepository;

import com.medicalcrm.backend.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final DoctorRepository doctorRepository;

    // DOCTOR

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalReceivedByDoctor(Long doctorId){

        Doctor doctor = getDoctorEntity(doctorId);
        checkDoctorOwnership(doctor);

        BigDecimal received = Optional.ofNullable(
                paymentRepository.sumPaymentsToDoctor(doctorId)
        ).orElse(BigDecimal.ZERO);

        log.info("Doctor {} total received: {}", doctorId, received);

        return received;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpectedByDoctor(Long doctorId){

        Doctor doctor = getDoctorEntity(doctorId);
        checkDoctorOwnership(doctor);

        BigDecimal expected = Optional.ofNullable(
                appointmentRepository.sumAppointmentPricesToDoctor(doctorId)
        ).orElse(BigDecimal.ZERO);

        log.info("Doctor {} total expected: {}", doctorId, expected);

        return expected;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingByDoctor(Long doctorId){

        Doctor doctor = getDoctorEntity(doctorId);
        checkDoctorOwnership(doctor);

        BigDecimal received = getTotalReceivedByDoctor(doctorId);
        BigDecimal expected = getTotalExpectedByDoctor(doctorId);

        BigDecimal pending = expected.subtract(received);

        if (pending.compareTo(BigDecimal.ZERO) < 0) {

            log.error(
                    "Negative pending detected for doctor {} (total={}, received={})",
                    doctorId, expected, received
            );

            pending = BigDecimal.ZERO;
        }

        log.info("Doctor {} total pending: {}", doctorId, pending);

        return pending;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPaidAppointmentsByDoctor(Long doctorId) {

        Doctor doctor = getDoctorEntity(doctorId);
        checkDoctorOwnership(doctor);

        List<Appointment> appointments =
                appointmentRepository.findByDoctorId(doctorId);

        return appointments.stream()
                .filter(a -> {
                    BigDecimal paid =
                            paymentRepository.sumAmountByAppointmentId(a.getId());
                    if (paid == null) {
                        paid = BigDecimal.ZERO;
                    }
                    BigDecimal price = a.getService().getPrice();
                    return paid.compareTo(price) >= 0;
                })
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUnpaidAppointmentsByDoctor(Long doctorId){

        Doctor doctor = getDoctorEntity(doctorId);
        checkDoctorOwnership(doctor);

        List<Appointment> appointments =
                appointmentRepository.findByDoctorId(doctorId);

        return appointments.stream()
                .filter(a -> {
                    BigDecimal paid =
                            paymentRepository.sumAmountByAppointmentId(a.getId());
                    if (paid == null) {
                        paid = BigDecimal.ZERO;
                    }
                    BigDecimal price = a.getService().getPrice();
                    return paid.compareTo(price) < 0;
                })
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    // PATIENT

    @Override
    public PaymentResponse makePayment(Long patientId, CreatePaymentRequest request) {

        Patient patient = getPatientEntity(patientId);
        checkPatientOwnership(patient);

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patientId)) {
            throw new BusinessException("This appointment does not belong to patient");
        }

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

        Patient patient = getPatientEntity(patientId);
        checkPatientOwnership(patient);

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

        Patient patient = getPatientEntity(patientId);
        checkPatientOwnership(patient);

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

        BigDecimal totalPaid = Optional.ofNullable(
                paymentRepository.sumPaymentsByPatient(patientId)
        ).orElse(BigDecimal.ZERO);

        log.info("Patient {} total paid: {}", patientId, totalPaid);

        return totalPaid;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingByPatient(Long patientId) {

        Patient patient = getPatientEntity(patientId);
        checkPatientOwnership(patient);

        BigDecimal paid = getTotalPaidByPatient(patientId);
        BigDecimal expected = getTotalExpectedByPatient(patientId);

        BigDecimal pending = expected.subtract(paid);

        if (pending.compareTo(BigDecimal.ZERO) < 0) {
            pending = BigDecimal.ZERO;
        }

        log.info("Patient {} pending: {}", patientId, pending);


        return pending;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpectedByPatient(Long patientId) {

        Patient patient = getPatientEntity(patientId);
        checkPatientOwnership(patient);

        BigDecimal expected = Optional.ofNullable(
                appointmentRepository.sumAppointmentPricesByPatient(patientId)
        ).orElse(BigDecimal.ZERO);

        log.info("Patient {} total expected: {}", patientId, expected);

        return expected;
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

    // HELPERS
    private Patient getPatientEntity(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found"));
    }

    private Doctor getDoctorEntity(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private String getCurrentUsername() {
        return getAuthentication().getName();
    }

    private boolean isAdmin() {
        return getAuthentication().getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private void checkPatientOwnership(Patient patient) {

        if (!isAdmin() &&
                !patient.getUser().getUsername().equals(getCurrentUsername())) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private void checkDoctorOwnership(Doctor doctor) {

        if (!isAdmin() &&
                !doctor.getUser().getUsername().equals(getCurrentUsername())) {
            throw new AccessDeniedException("Access denied");
        }
    }

}
