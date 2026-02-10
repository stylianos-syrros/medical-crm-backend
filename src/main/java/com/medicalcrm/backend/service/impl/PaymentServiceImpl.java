package com.medicalcrm.backend.service.impl;

import com.medicalcrm.backend.exception.BusinessException;
import com.medicalcrm.backend.exception.NotFoundException;

import com.medicalcrm.backend.model.Appointment;
import com.medicalcrm.backend.model.Patient;
import com.medicalcrm.backend.model.Payment;

import com.medicalcrm.backend.model.PaymentMethod;
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
    public Payment makePayment(Long patientId,
                               Long appointmentId,
                               BigDecimal amount,
                               PaymentMethod method){

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payment amount must be positive");
        }

        if (method == null){
            throw new BusinessException("Payment method is required");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(()->
                        new NotFoundException(("Appointment not found")));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() ->
                        new NotFoundException("Patient not found"));

        if (!appointment.getPatient().getId().equals(patientId)){
            throw new BusinessException(
                    "You can only pay your own appointments");
        }

        BigDecimal totalPaid = paymentRepository.sumAmountByAppointmentId(appointmentId);
        BigDecimal price = appointment.getService().getPrice();

        BigDecimal remaining = price.subtract(totalPaid);

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "Appointment already fully paid");
        }

        if (amount.compareTo(remaining) > 0) {
            throw new BusinessException(
                    "Amount exceeds remaining amount");
        }

        Payment payment = new Payment();

        payment.setAppointment(appointment);
        payment.setPaidBy(patient);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setPaidAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        log.info("Patient {} paid {} for appointment {} via {}",
                patientId, amount, appointmentId, method);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getPaidAppointmentsByPatient(Long patientId) {

        List<Appointment> appointments =
                appointmentRepository.findByPatientId(patientId);

        return appointments.stream()
                .filter(a -> {

                    BigDecimal paid =
                            paymentRepository
                                    .sumAmountByAppointmentId(a.getId());

                    BigDecimal price =
                            a.getService().getPrice();

                    return paid.compareTo(price) >= 0;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)

    public List<Appointment> getUnpaidAppointmentsByPatient(Long patientId) {

        List<Appointment> appointments =
                appointmentRepository.findByPatientId(patientId);

        return appointments.stream()
                .filter(a -> {

                    BigDecimal paid =
                            paymentRepository
                                    .sumAmountByAppointmentId(a.getId());

                    BigDecimal price =
                            a.getService().getPrice();

                    return paid.compareTo(price) < 0;
                })
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
    public List<Payment> getAllPayments(){
        return paymentRepository.findAll();

    }
}
