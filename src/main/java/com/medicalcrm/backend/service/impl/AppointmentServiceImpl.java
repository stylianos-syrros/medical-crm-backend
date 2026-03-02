package com.medicalcrm.backend.service.impl;

import com.medicalcrm.backend.dto.request.CreateAppointmentRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentNotesRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentScheduleRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.exception.BusinessException;
import com.medicalcrm.backend.exception.NotFoundException;
import com.medicalcrm.backend.mapper.AppointmentMapper;
import com.medicalcrm.backend.model.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.medicalcrm.backend.repository.*;

import com.medicalcrm.backend.service.AppointmentService;

import lombok.RequiredArgsConstructor; //?
import lombok.extern.slf4j.Slf4j; //?

import org.springframework.stereotype.Service; //?
import org.springframework.transaction.annotation.Transactional; //?

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final UserRepository userRepository;

    // PATIENT
    @Override
    public AppointmentResponse bookAppointment(CreateAppointmentRequest request) {

        Patient patient = getCurrentPatientEntity();

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        MedicalService service = medicalServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        Appointment appointment = AppointmentMapper.toEntity(request, patient, doctor, service);
        Appointment saved = appointmentRepository.save(appointment);

        log.info("Patient {} booked appointment {}", patient.getId(), saved.getId());

        return AppointmentMapper.toResponse(saved);
    }

    @Override
    public void cancelAppointmentByPatient(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new NotFoundException("Appointment not found"));

        Patient patient = getCurrentPatientEntity();

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new BusinessException("Not your appointment");
        }

        if (paymentRepository.existsByAppointmentId(appointmentId)) {
            throw new BusinessException("Cannot cancel paid appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);

        log.info("Patient {} cancelled appointment {}", patient.getId(), appointmentId);
    }

    @Override
    public void rescheduleAppointment(Long appointmentId,
                                      UpdateAppointmentScheduleRequest request) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new NotFoundException("Appointment not found"));

        Patient patient = getCurrentPatientEntity();

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new BusinessException("Not your appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessException("Only scheduled appointments can be rescheduled");
        }

        AppointmentMapper.updateSchedule(appointment, request);

        log.info("Patient {} rescheduled appointment {}", patient.getId(), appointmentId);
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUpcomingAppointmentsForPatient() {

        Patient patient = getCurrentPatientEntity();

        return appointmentRepository.findByPatientIdAndStatus(
                patient.getId(),
                AppointmentStatus.SCHEDULED)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsHistoryForPatient(){

        Patient patient = getCurrentPatientEntity();

        return appointmentRepository.findByPatientIdAndStatus(
                patient.getId(),
                AppointmentStatus.COMPLETED)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    // DOCTOR

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUpcomingAppointmentsForDoctor() {

        Doctor doctor = getCurrentDoctorEntity();

        return appointmentRepository
                .findByDoctorIdAndStatus(
                        doctor.getId(),
                        AppointmentStatus.SCHEDULED)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsHistoryForDoctor() {

        Doctor doctor = getCurrentDoctorEntity();

        return appointmentRepository
                .findByDoctorIdAndStatus(
                        doctor.getId(),
                        AppointmentStatus.COMPLETED)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsForDoctorByStatus(AppointmentStatus status) {

        Doctor doctor = getCurrentDoctorEntity();

        return appointmentRepository
                .findByDoctorIdAndStatus(doctor.getId(), status)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    public void cancelAppointmentByDoctor(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new NotFoundException("Appointment not found"));

        Doctor doctor = getCurrentDoctorEntity();

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        if (paymentRepository.existsByAppointmentId(appointmentId)){
            throw new BusinessException("Cannot cancel appointment with payments");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);

        log.info("Doctor {} cancelled appointment {}",doctor.getId(),appointmentId);
    }

    @Override
    public void completeAppointmentByDoctor(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(()->
                        new NotFoundException("Appointment not found"));

        Doctor doctor = getCurrentDoctorEntity();

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        BigDecimal totalPaid = paymentRepository.sumAmountByAppointmentId(appointmentId);

        if (totalPaid == null) {
            totalPaid = BigDecimal.ZERO;
        }

        BigDecimal price = appointment.getService().getPrice();

        if (totalPaid.compareTo(price) < 0){
            throw new BusinessException(
                    "Appointment cannot be completed unless fully paid");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);

        log.info("Doctor {} completed appointment {}", doctor.getId(), appointmentId);
    }

    @Override
    public void updateNotesByDoctor(Long appointmentId,
                                         UpdateAppointmentNotesRequest request) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        Doctor doctor = getCurrentDoctorEntity();

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessException("Notes can be updated only for scheduled appointments");
        }

        AppointmentMapper.updateNotes(appointment, request);

        log.info("Doctor {} updated notes for appointment {}",
                doctor.getId(), appointmentId);
    }


    // ADMIN

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() {

        return appointmentRepository.findAll()
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByDate(LocalDate date) {

        return appointmentRepository.findByAppointmentDate(date)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByStatus(AppointmentStatus status) {

        return appointmentRepository.findByStatus(status)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
        public List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId) {

        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByPatient(Long patientId) {

        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    // HELPERS


    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private String getCurrentUsername() {
        return getAuthentication().getName();
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
