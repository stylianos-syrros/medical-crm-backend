package com.medicalcrm.backend.service.impl;

import com.medicalcrm.backend.dto.request.CreatePatientRequest;
import com.medicalcrm.backend.dto.request.UpdatePatientRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.dto.response.DoctorResponse;
import com.medicalcrm.backend.dto.response.PatientResponse;
import com.medicalcrm.backend.mapper.AppointmentMapper;
import com.medicalcrm.backend.mapper.DoctorMapper;
import com.medicalcrm.backend.mapper.PatientMapper;
import com.medicalcrm.backend.service.PatientService;

import com.medicalcrm.backend.exception.*;
import com.medicalcrm.backend.model.*;
import com.medicalcrm.backend.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public PatientResponse createMyProfile(CreatePatientRequest request) {
        String username = getCurrentUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        patientRepository.findByUserId(user.getId()).ifPresent(p -> {
            throw new BusinessException("Patient profile already exists");
        });

        if (patientRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Phone number already in use");
        }

        Patient patient = PatientMapper.toEntity(request, user);
        Patient saved = patientRepository.save(patient);

        log.info("Patient profile created for user {}", user.getId());

        return PatientMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getMyProfile() {

        Patient patient = getCurrentPatientEntity();
        return PatientMapper.toResponse(patient);
    }

    @Override
    public PatientResponse updateMyProfile(UpdatePatientRequest request) {

        Patient patient = getCurrentPatientEntity();
        
        if (patientRepository.existsByPhoneAndIdNot(request.getPhone(), patient.getId())) {
            throw new BusinessException("Phone number already in use");
        }

        PatientMapper.updateEntity(patient, request);

        log.info("Patient {} updated own profile", patient.getId());

        return PatientMapper.toResponse(patient);
    }


    // DOCTOR && APPOINTMENT
    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getMyDoctors(){

        Long patientId = getCurrentPatientEntity().getId();

        return appointmentRepository
                .findDistinctDoctorsByPatientId(patientId)
                .stream()
                .map(DoctorMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(){

        Long patientId = getCurrentPatientEntity().getId();

        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointmentsHistory(){

        Long patientId = getCurrentPatientEntity().getId();

        return appointmentRepository
                .findByPatientIdAndStatus(patientId, AppointmentStatus.COMPLETED)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyUpcomingAppointments(){

        Long patientId = getCurrentPatientEntity().getId();

        return appointmentRepository
                .findByPatientIdAndStatus(patientId, AppointmentStatus.SCHEDULED)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }


    @Override
    public void cancelAppointment(Long appointmentId){
            Patient patient = getCurrentPatientEntity();
            Long patientId = patient.getId();

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

    // HELPERS

    private Patient getCurrentPatientEntity() {
        String username = getCurrentUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Patient profile not found"));
    }


    private Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new BusinessException("No authentication context available");
        }
        return auth;
    }

    private String getCurrentUsername() {
        return getAuthentication().getName();
    }
}