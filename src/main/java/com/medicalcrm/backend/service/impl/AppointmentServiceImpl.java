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

import lombok.RequiredArgsConstructor; 
import lombok.extern.slf4j.Slf4j; 

import org.springframework.stereotype.Service; 
import org.springframework.transaction.annotation.Transactional; 

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final UserRepository userRepository;

    // PATIENT
    @Override
    public AppointmentResponse bookAppointment(CreateAppointmentRequest request) {

        Patient patient = getCurrentPatientEntity();

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        MedicalService service = medicalServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        validateAppointmentTimeSlot(request.getAppointmentTime());
        validateAppointmentDateTimeInFuture(request.getAppointmentDate(), request.getAppointmentTime());
        validateNoScheduleCollisions(
                patient.getId(),
                doctor.getId(),
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                service.getDuration(),
                null);

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

        validateAppointmentTimeSlot(request.getAppointmentTime());
        validateAppointmentDateTimeInFuture(request.getAppointmentDate(), request.getAppointmentTime());
        validateNoScheduleCollisions(
                patient.getId(),
                appointment.getDoctor().getId(),
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                appointment.getService().getDuration(),
                appointmentId);

        AppointmentMapper.updateSchedule(appointment, request);

        log.info("Patient {} rescheduled appointment {}", patient.getId(), appointmentId);
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUpcomingAppointmentsForPatient() {

        Patient patient = getCurrentPatientEntity();
        LocalDateTime now = LocalDateTime.now();

        return appointmentRepository.findByPatientId(patient.getId())
                .stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .filter(a -> isAppointmentInFuture(a, now))
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsHistoryForPatient(){

        Patient patient = getCurrentPatientEntity();
        LocalDateTime now = LocalDateTime.now();

        return appointmentRepository.findByPatientId(patient.getId())
                .stream()
                .filter(a -> isAppointmentInHistory(a, now))
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorScheduledAppointmentsByDateForPatient(
            Long doctorId,
            LocalDate date) {

        if (!doctorRepository.existsById(doctorId)) {
            throw new NotFoundException("Doctor not found");
        }

        return appointmentRepository
                .findByDoctorIdAndAppointmentDateAndStatus(
                        doctorId,
                        date,
                        AppointmentStatus.SCHEDULED)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    public void updateNotesByPatient(Long appointmentId,
                                     UpdateAppointmentNotesRequest request) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        Patient patient = getCurrentPatientEntity();

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new BusinessException("Not your appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessException("Notes can be updated only for scheduled appointments");
        }

        appointment.setPatientNotes(request.getNotes());

        log.info("Patient {} updated notes for appointment {}",
                patient.getId(), appointmentId);
    }

    // DOCTOR

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUpcomingAppointmentsForDoctor() {

        Doctor doctor = getCurrentDoctorEntity();
        LocalDateTime now = LocalDateTime.now();

        return appointmentRepository
                .findByDoctorId(doctor.getId())
                .stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .filter(a -> isAppointmentInFuture(a, now))
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsHistoryForDoctor() {

        Doctor doctor = getCurrentDoctorEntity();
        LocalDateTime now = LocalDateTime.now();

        return appointmentRepository
                .findByDoctorId(doctor.getId())
                .stream()
                .filter(a -> isAppointmentInHistory(a, now))
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

        LocalDateTime appointmentDateTime = LocalDateTime.of(
            appointment.getAppointmentDate(),
            appointment.getAppointmentTime()
        );
        if (appointmentDateTime.isAfter(LocalDateTime.now())) {
            throw new BusinessException("Cannot complete a future appointment");
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

    private void validateAppointmentTimeSlot(LocalTime appointmentTime) {
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(20, 0);

        if (appointmentTime.isBefore(start) || appointmentTime.isAfter(end)) {
            throw new BusinessException("Appointments must be between 09:00 and 20:00");
        }

        boolean minuteAligned = appointmentTime.getMinute() == 0 || appointmentTime.getMinute() == 30;
        boolean secondAligned = appointmentTime.getSecond() == 0 && appointmentTime.getNano() == 0;

        if (!minuteAligned || !secondAligned) {
            throw new BusinessException("Appointments must be on 30-minute slots");
        }
    }

    private void validateNoScheduleCollisions(
            Long patientId,
            Long doctorId,
            LocalDate date,
            LocalTime time,
            Integer durationMinutes,
            Long excludeAppointmentId) {
        int requestedDuration = normalizeDuration(durationMinutes);
        LocalTime requestedEnd = time.plusMinutes(requestedDuration);

        List<Appointment> patientAppointments = appointmentRepository
                .findByPatientIdAndAppointmentDateAndStatus(
                        patientId,
                        date,
                        AppointmentStatus.SCHEDULED);

        List<Appointment> doctorAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentDateAndStatus(
                        doctorId,
                        date,
                        AppointmentStatus.SCHEDULED);

        boolean patientHasCollision = patientAppointments.stream()
                .filter(a -> excludeAppointmentId == null || !a.getId().equals(excludeAppointmentId))
                .anyMatch(a -> isOverlapping(
                        time,
                        requestedEnd,
                        a.getAppointmentTime(),
                        a.getAppointmentTime().plusMinutes(normalizeDuration(a.getService().getDuration()))));

        boolean doctorHasCollision = doctorAppointments.stream()
                .filter(a -> excludeAppointmentId == null || !a.getId().equals(excludeAppointmentId))
                .anyMatch(a -> isOverlapping(
                        time,
                        requestedEnd,
                        a.getAppointmentTime(),
                        a.getAppointmentTime().plusMinutes(normalizeDuration(a.getService().getDuration()))));

        if (patientHasCollision) {
            throw new BusinessException("You already have an appointment at this date and time");
        }

        if (doctorHasCollision) {
            throw new BusinessException("Doctor is not available at this date and time");
        }
    }

    private int normalizeDuration(Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes <= 0) {
            return 30;
        }
        return durationMinutes;
    }

    private boolean isOverlapping(
            LocalTime startA,
            LocalTime endA,
            LocalTime startB,
            LocalTime endB) {

        return startA.isBefore(endB) && startB.isBefore(endA);
    }

    private void validateAppointmentDateTimeInFuture(LocalDate date, LocalTime time) {
        LocalDateTime requested = LocalDateTime.of(date, time);
        if (!requested.isAfter(LocalDateTime.now())) {
            throw new BusinessException("Appointment date/time must be in the future");
        }
    }

    private boolean isAppointmentInFuture(Appointment appointment, LocalDateTime now) {
        if (appointment.getAppointmentDate() == null || appointment.getAppointmentTime() == null) {
            return false;
        }
        LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime());
        return appointmentDateTime.isAfter(now);
    }

    private boolean isAppointmentInHistory(Appointment appointment, LocalDateTime now) {
        AppointmentStatus status = appointment.getStatus();

        if (status == AppointmentStatus.COMPLETED || status == AppointmentStatus.CANCELLED) {
            return true;
        }

        if (status == AppointmentStatus.SCHEDULED) {
            if (appointment.getAppointmentDate() == null || appointment.getAppointmentTime() == null) {
                return false;
            }
            LocalDateTime appointmentDateTime = LocalDateTime.of(
                    appointment.getAppointmentDate(),
                    appointment.getAppointmentTime());
            return !appointmentDateTime.isAfter(now);
        }

        return false;
    }

}
