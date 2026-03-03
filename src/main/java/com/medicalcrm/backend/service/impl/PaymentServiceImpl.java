package com.medicalcrm.backend.service.impl;

import com.medicalcrm.backend.dto.request.CreatePaymentRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.dto.response.PaymentResponse;
import com.medicalcrm.backend.exception.BusinessException;
import com.medicalcrm.backend.exception.NotFoundException;
import com.medicalcrm.backend.mapper.AppointmentMapper;
import com.medicalcrm.backend.mapper.PaymentMapper;
import com.medicalcrm.backend.model.Appointment;
import com.medicalcrm.backend.model.AppointmentStatus;
import com.medicalcrm.backend.model.Doctor;
import com.medicalcrm.backend.model.Patient;
import com.medicalcrm.backend.model.Payment;
import com.medicalcrm.backend.model.PaymentStatus;
import com.medicalcrm.backend.model.User;
import com.medicalcrm.backend.repository.AppointmentRepository;
import com.medicalcrm.backend.repository.DoctorRepository;
import com.medicalcrm.backend.repository.PatientRepository;
import com.medicalcrm.backend.repository.PaymentRepository;
import com.medicalcrm.backend.repository.UserRepository;
import com.medicalcrm.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    // DOCTOR

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalReceivedByDoctor() {
        Doctor doctor = getCurrentDoctorEntity();
        return Optional.ofNullable(paymentRepository.sumPaymentsToDoctor(doctor.getId()))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpectedByDoctor() {
        Doctor doctor = getCurrentDoctorEntity();
        return Optional.ofNullable(appointmentRepository.sumAppointmentPricesToDoctor(doctor.getId()))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingByDoctor() {
        BigDecimal received = getTotalReceivedByDoctor();
        BigDecimal expected = getTotalExpectedByDoctor();
        BigDecimal pending = expected.subtract(received);
        return pending.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : pending;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPaidAppointmentsByDoctor() {
        Doctor doctor = getCurrentDoctorEntity();
        return appointmentRepository.findByDoctorId(doctor.getId()).stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .filter(a -> Optional.ofNullable(paymentRepository.sumAmountByAppointmentId(a.getId()))
                        .orElse(BigDecimal.ZERO)
                        .compareTo(a.getService().getPrice()) >= 0)
                .map(this::toAppointmentResponseWithPaymentInfo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUnpaidAppointmentsByDoctor() {
        Doctor doctor = getCurrentDoctorEntity();
        return appointmentRepository.findByDoctorId(doctor.getId()).stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .filter(a -> Optional.ofNullable(paymentRepository.sumAmountByAppointmentId(a.getId()))
                        .orElse(BigDecimal.ZERO)
                        .compareTo(a.getService().getPrice()) < 0)
                .map(this::toAppointmentResponseWithPaymentInfo)
                .toList();
    }

    // PATIENT

    @Override
    public PaymentResponse makePayment(CreatePaymentRequest request) {
        Patient patient = getCurrentPatientEntity();

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new BusinessException("This appointment does not belong to patient");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException("Cannot pay a cancelled appointment");
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
        payment.setPaidAt(LocalDateTime.now());

        BigDecimal newTotalPaid = totalPaid.add(paymentAmount);
        payment.setStatus(newTotalPaid.compareTo(price) >= 0 ? PaymentStatus.PAID : PaymentStatus.PARTIAL);

        Payment saved = paymentRepository.save(payment);

        log.info("Patient {} paid {} for appointment {}",
                patient.getId(), paymentAmount, appointment.getId());

        return PaymentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPaidAppointmentsByPatient() {
        Patient patient = getCurrentPatientEntity();
        return appointmentRepository.findByPatientId(patient.getId()).stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .filter(a -> Optional.ofNullable(paymentRepository.sumAmountByAppointmentId(a.getId()))
                        .orElse(BigDecimal.ZERO)
                        .compareTo(a.getService().getPrice()) >= 0)
                .map(this::toAppointmentResponseWithPaymentInfo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUnpaidAppointmentsByPatient() {
        Patient patient = getCurrentPatientEntity();
        return appointmentRepository.findByPatientId(patient.getId()).stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .filter(a -> Optional.ofNullable(paymentRepository.sumAmountByAppointmentId(a.getId()))
                        .orElse(BigDecimal.ZERO)
                        .compareTo(a.getService().getPrice()) < 0)
                .map(this::toAppointmentResponseWithPaymentInfo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidByPatient() {
        Patient patient = getCurrentPatientEntity();
        return Optional.ofNullable(paymentRepository.sumPaymentsByPatient(patient.getId()))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingByPatient() {
        BigDecimal paid = getTotalPaidByPatient();
        BigDecimal expected = getTotalExpectedByPatient();
        BigDecimal pending = expected.subtract(paid);
        return pending.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : pending;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpectedByPatient() {
        Patient patient = getCurrentPatientEntity();
        return Optional.ofNullable(appointmentRepository.sumAppointmentPricesByPatient(patient.getId()))
                .orElse(BigDecimal.ZERO);
    }

    // ADMIN

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(PaymentMapper::toResponse)
                .toList();
    }

    // HELPERS

    private AppointmentResponse toAppointmentResponseWithPaymentInfo(Appointment appointment) {
        AppointmentResponse response = AppointmentMapper.toResponse(appointment);
        BigDecimal totalPaid = Optional.ofNullable(
                paymentRepository.sumAmountByAppointmentId(appointment.getId())
        ).orElse(BigDecimal.ZERO);
        BigDecimal price = Optional.ofNullable(appointment.getService().getPrice()).orElse(BigDecimal.ZERO);
        BigDecimal pending = price.subtract(totalPaid);
        if (pending.compareTo(BigDecimal.ZERO) < 0) {
            pending = BigDecimal.ZERO;
        }
        response.setTotalPaid(totalPaid);
        response.setPendingAmount(pending);
        return response;
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private User getCurrentUserEntity() {
        return userRepository.findByUsername(getCurrentUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Patient getCurrentPatientEntity() {
        User user = getCurrentUserEntity();
        return patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Patient profile not found"));
    }

    private Doctor getCurrentDoctorEntity() {
        User user = getCurrentUserEntity();
        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Doctor profile not found"));
    }
}
