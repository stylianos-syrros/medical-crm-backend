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
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public PatientResponse createMyProfile(CreatePatientRequest request) {
        String username = getCurrentUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        patientRepository.findByUserId(user.getId()).ifPresent(p -> {
            throw new BusinessException("Patient profile already exists");
        });

        if (phoneUsedByAnotherUserForPatient(request.getPhone(), -1L, user.getId())) {
            throw new BusinessException("Phone number already in use");
        }

        Patient patient = PatientMapper.toEntity(request, user);

        log.info("Create patient profile attempt: userId={}, phone={}, phoneExists={}",
        user.getId(), request.getPhone(), patientRepository.existsByPhone(request.getPhone()));

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
        
        String newPhone = request.getPhone();
        String currentPhone = patient.getPhone();

        if (!newPhone.equals(currentPhone) &&
            phoneUsedByAnotherUserForPatient(newPhone, patient.getId(), patient.getUser().getId())) {
            throw new BusinessException("Phone number already in use");
        }

        PatientMapper.updateEntity(patient, request);

        log.info("Patient {} updated own profile", patient.getId());

        return PatientMapper.toResponse(patient);
    }

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
    public List<DoctorResponse> getAllDoctorsForBooking() {

        return doctorRepository.findAll()
                .stream()
                .map(DoctorMapper::toResponse)
                .toList();
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

    private boolean phoneUsedByAnotherUserForPatient(String phone, Long currentPatientId, Long currentUserId) {
        boolean usedByOtherPatient = patientRepository.existsByPhoneAndIdNot(phone, currentPatientId);

        boolean usedByOtherDoctor = doctorRepository.findByPhone(phone)
                .map(d -> !d.getUser().getId().equals(currentUserId))
                .orElse(false);

        return usedByOtherPatient || usedByOtherDoctor;
    }

}
