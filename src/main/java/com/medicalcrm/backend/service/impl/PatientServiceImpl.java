package com.medicalcrm.backend.service.impl;

import com.medicalcrm.backend.dto.request.CreatePatientRequest;
import com.medicalcrm.backend.dto.request.UpdatePatientRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.dto.response.DoctorResponse;
import com.medicalcrm.backend.dto.response.PatientResponse;
import com.medicalcrm.backend.mapper.AppointmentMapper;
import com.medicalcrm.backend.mapper.DoctorMapper;
import com.medicalcrm.backend.mapper.PatientMapper;
import com.medicalcrm.backend.model.Patient;
import com.medicalcrm.backend.service.PatientService;

import com.medicalcrm.backend.exception.*;
import com.medicalcrm.backend.model.*;
import com.medicalcrm.backend.model.AppointmentStatus;
import com.medicalcrm.backend.repository.*;
import com.medicalcrm.backend.service.PatientService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Override
    public PatientResponse createPatient(CreatePatientRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Patient patient = PatientMapper.toEntity(request, user);

        Patient saved = patientRepository.save(patient);

        log.info("Patient profile created for user {}", user.getId());

        return PatientMapper.toResponse(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public PatientResponse getProfile(Long patientId) {

        Patient patient = getPatientEntity(patientId);

        return PatientMapper.toResponse(patient);
    }

    @Override
    public PatientResponse updateProfile(Long patientId,
                                         UpdatePatientRequest request) {

        Patient patient = getPatientEntity(patientId);

        PatientMapper.updateEntity(patient, request);

        log.info("Patient {} updated profile", patientId);

        return PatientMapper.toResponse(patient);
    }


    // DOCTOR && APPOINTMENT
    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getMyDoctors(Long patientId){

        return appointmentRepository
                .findDistinctDoctorsByPatientId(patientId)
                .stream()
                .map(DoctorMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(Long patientId){

        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointmentHistory(Long patientId){

        return appointmentRepository
                .findByPatientIdAndStatus(patientId, AppointmentStatus.COMPLETED)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyUpcomingAppointments(Long patientId){

        return appointmentRepository
                .findByPatientIdAndStatus(patientId, AppointmentStatus.SCHEDULED)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }


    @Override
    public void cancelAppointment(Long patientId, Long appointmentId){
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(()->
                        new NotFoundException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patientId)){
            throw new BusinessException("Not your appointment");
        }

        boolean hasPayments = paymentRepository.existsByAppointmentId(appointmentId);

        if (hasPayments){
            throw new BusinessException("Cannot cancel paid appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);

        log.info("Patient {} cancelled appointment {}",patientId, appointmentId);
    }

    private Patient getPatientEntity(Long patientId) {

        return patientRepository.findById(patientId)
                .orElseThrow(() ->
                        new NotFoundException("Patient not found"));
    }
}
