package com.medicalcrm.backend.service.impl;

import com.medicalcrm.backend.exception.BusinessException;
import com.medicalcrm.backend.exception.NotFoundException;
import com.medicalcrm.backend.model.*;
import com.medicalcrm.backend.model.AppointmentStatus;

import com.medicalcrm.backend.repository.*;

import com.medicalcrm.backend.service.AppointmentService;

import lombok.RequiredArgsConstructor; //?
import lombok.extern.slf4j.Slf4j; //?

import org.springframework.stereotype.Service; //?
import org.springframework.transaction.annotation.Transactional; //?

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl  implements AppointmentService{

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalServiceRepository medicalServiceRepository;
    private final PaymentRepository paymentRepository;

    // PATIENT

    @Override
    public Appointment bookAppointment(Long patientId,
                                       Long doctorId,
                                       Long serviceId,
                                       LocalDate date,
                                       LocalTime time) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() ->
                        new NotFoundException("Patient not found"));


        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new NotFoundException("Doctor not found"));

        MedicalService service = medicalServiceRepository.findById(serviceId)
                .orElseThrow(() ->
                        new NotFoundException("Service not found"));

        Appointment appointment = new Appointment();

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setService(service);
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setNotes(null);

        Appointment saved = appointmentRepository.save(appointment);

        log.info("Patient {} booked appointment {}", patientId, saved.getId());

        return saved;
    }

    @Override
    public void cancelAppointment(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new NotFoundException("Appointment not found"));

        if (paymentRepository.existsByAppointmentId(appointmentId)) {
            log.warn("Patient cancel denied for appointment {}", appointmentId);

            throw new BusinessException(
                    "Cannot cancel appointment with payments");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);

        log.info("Appointment {} cancelled by patient", appointmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointmentsForPatient(Long patientId) {

        return appointmentRepository.findByPatientIdAndStatus(
                patientId,
                AppointmentStatus.SCHEDULED);

    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentHistoryForPatient(Long patientId){

        return appointmentRepository.findByPatientIdAndStatus(
                patientId,
                AppointmentStatus.COMPLETED);
    }

    // DOCTOR

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointmentsForDoctor(Long doctorId) {

        return appointmentRepository
                .findByDoctorIdAndStatus(
                        doctorId,
                        AppointmentStatus.SCHEDULED
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentHistoryForDoctor(Long doctorId) {

        return appointmentRepository
                .findByDoctorIdAndStatus(
                        doctorId,
                        AppointmentStatus.COMPLETED
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsForDoctorByStatus(Long doctorId,
                                                              AppointmentStatus status) {

        return appointmentRepository
                .findByDoctorIdAndStatus(doctorId, status);
    }

    @Override
    public void cancelAppointmentByDoctor(Long doctorId,
                                          Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new NotFoundException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new BusinessException("You cannot cancel another doctor's appointment");
        }

        if (paymentRepository.existsByAppointmentId(appointmentId)){
            throw new BusinessException("Cannot cancel appointment with payments");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);

        log.info("Doctor {} cancelled appointment {}",doctorId, appointmentId);
    }

    @Override
    public void completeAppointmentByDoctor(Long doctorId,
                                            Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(()->
                        new NotFoundException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new BusinessException(
                    "You cannot complete another doctor's appointment");
        }

        BigDecimal totalPaid = paymentRepository.sumAmountByAppointmentId(appointmentId);

        BigDecimal price = appointment.getService().getPrice();

        if (totalPaid.compareTo(price) < 0){
            throw new BusinessException(
                    "Appointment cannot be completed unless fully paid");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);

    }

    @Override
    public void addOrUpdateNotesByDoctor(Long doctorId,
                                         Long appointmentId,
                                         String notes) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new BusinessException("Not your appointment");
        }

        appointment.setNotes(notes);

        log.info("Doctor {} updated notes for appointment {}",
                doctorId, appointmentId);
    }


    // ADMIN

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {

        return appointmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByDate(LocalDate date) {

        return appointmentRepository.findByAppointmentDate(date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {

        return appointmentRepository.findByStatus(status);
    }

    @Override
    public void updateNotesByAdmin(Long appointmentId,
                                   String notes) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        appointment.setNotes(notes);

        log.info("Admin updated notes for appointment {}",
                appointmentId);
    }

    @Override
    @Transactional(readOnly = true)
        public List<Appointment> getAppointmentsByDoctor(Long doctorId) {

        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByPatient(Long patientId) {

        return appointmentRepository.findByPatientId(patientId);
    }

}
