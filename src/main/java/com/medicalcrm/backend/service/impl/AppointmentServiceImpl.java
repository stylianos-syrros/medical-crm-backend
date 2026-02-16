package com.medicalcrm.backend.service.impl;

import com.medicalcrm.backend.dto.request.CreateAppointmentRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentNotesRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentScheduleRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.exception.BusinessException;
import com.medicalcrm.backend.exception.NotFoundException;
import com.medicalcrm.backend.mapper.AppointmentMapper;
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
    public AppointmentResponse bookAppointment(Long patientId,
                                               CreateAppointmentRequest request) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() ->
                        new NotFoundException("Patient not found"));


        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() ->
                        new NotFoundException("Doctor not found"));

        MedicalService service = medicalServiceRepository.findById(request.getServiceId())
                .orElseThrow(() ->
                        new NotFoundException("Service not found"));

        Appointment appointment = AppointmentMapper.toEntity(request, patient, doctor, service);

        Appointment saved = appointmentRepository.save(appointment);

        log.info("Patient {} booked appointment {}", patientId, saved.getId());

        return AppointmentMapper.toResponse(saved);
    }

    @Override
    public void cancelAppointment(Long patientId, Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new NotFoundException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patientId)) {
            throw new BusinessException("Not your appointment");
        }

        boolean hasPayments =
                paymentRepository.existsByAppointmentId(appointmentId);

        if (hasPayments) {
            throw new BusinessException("Cannot cancel paid appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);

        log.info("Patient {} cancelled appointment {}", patientId, appointmentId);
    }

    @Override
    public void rescheduleAppointment(Long patientId,
                                      Long appointmentId,
                                      UpdateAppointmentScheduleRequest request) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new NotFoundException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patientId)) {
            throw new BusinessException("Not your appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessException("Only scheduled appointments can be rescheduled");
        }

        AppointmentMapper.updateSchedule(appointment, request);

        log.info("Patient {} rescheduled appointment {}", patientId, appointmentId);
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUpcomingAppointmentsForPatient(Long patientId) {

        return appointmentRepository.findByPatientIdAndStatus(
                patientId,
                AppointmentStatus.SCHEDULED)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();

    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentHistoryForPatient(Long patientId){

        return appointmentRepository.findByPatientIdAndStatus(
                patientId,
                AppointmentStatus.COMPLETED)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    // DOCTOR

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUpcomingAppointmentsForDoctor(Long doctorId) {

        return appointmentRepository
                .findByDoctorIdAndStatus(
                        doctorId,
                        AppointmentStatus.SCHEDULED)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentHistoryForDoctor(Long doctorId) {

        return appointmentRepository
                .findByDoctorIdAndStatus(
                        doctorId,
                        AppointmentStatus.COMPLETED)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsForDoctorByStatus(Long doctorId,
                                                              AppointmentStatus status) {

        return appointmentRepository
                .findByDoctorIdAndStatus(doctorId, status)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
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

        log.info("Doctor {} completed appointment {}", doctorId, appointmentId);


    }

    @Override
    public void updateNotesByDoctor(Long doctorId,
                                         Long appointmentId,
                                         UpdateAppointmentNotesRequest request) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new BusinessException("Not your appointment");
        }

        AppointmentMapper.updateNotes(appointment, request);

        log.info("Doctor {} updated notes for appointment {}",
                doctorId, appointmentId);
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

}
