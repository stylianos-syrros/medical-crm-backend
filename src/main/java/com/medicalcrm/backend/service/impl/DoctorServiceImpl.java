package com.medicalcrm.backend.service.impl;

import com.medicalcrm.backend.dto.request.CreateDoctorRequest;
import com.medicalcrm.backend.dto.request.UpdateDoctorRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.dto.response.DoctorResponse;
import com.medicalcrm.backend.dto.response.PatientResponse;
import com.medicalcrm.backend.exception.*;
import com.medicalcrm.backend.mapper.AppointmentMapper;
import com.medicalcrm.backend.mapper.DoctorMapper;
import com.medicalcrm.backend.mapper.PatientMapper;
import com.medicalcrm.backend.model.*;
import com.medicalcrm.backend.repository.*;
import com.medicalcrm.backend.service.DoctorService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;


    @Override
    public DoctorResponse createMyProfile(CreateDoctorRequest request){
        String username = getCurrentUsername();

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));

        doctorRepository.findByUserId(user.getId()).ifPresent(d -> {
            throw new BusinessException("Doctor profile already exists");
        });
        
        Doctor doctor = DoctorMapper.toEntity(request, user);

        if (phoneUsedByAnotherUserForDoctor(request.getPhone(), -1L, user.getId())) {
            throw new BusinessException("Phone number already in use");
        }



        Doctor saved = doctorRepository.save(doctor);

        log.info("Doctor profile created for user {}", user.getId());

        return DoctorMapper.toResponse(saved);
    }

    @Override
    public DoctorResponse getMyProfile(){

        String username = getCurrentUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Doctor profile not found"));

        return DoctorMapper.toResponse(doctor);
    }

    @Override
    public DoctorResponse updateMyProfile(UpdateDoctorRequest request) {

        Doctor doctor = getCurrentDoctorEntity();

        DoctorMapper.updateEntity(doctor, request);

        log.info("Doctor {} updated own profile", doctor.getId());

        String newPhone = request.getPhone();
        String currentPhone = doctor.getPhone();

        if (!newPhone.equals(currentPhone) &&
            phoneUsedByAnotherUserForDoctor(newPhone, doctor.getId(), doctor.getUser().getId())) {
            throw new BusinessException("Phone number already in use");
        }

        return DoctorMapper.toResponse(doctor);
    }

    // PATIENT 
    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getMyPatients(){

        Long doctorId = getCurrentDoctorEntity().getId();

        return appointmentRepository.findDistinctPatientsByDoctorId(doctorId)
                .stream()
                .map(PatientMapper::toResponse)
                .toList();
    }

    // HELPERS

    private Doctor getCurrentDoctorEntity() {
        String username = getCurrentUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Doctor profile not found"));
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

    private boolean phoneUsedByAnotherUserForDoctor(String phone, Long currentDoctorId, Long currentUserId) {
        boolean usedByOtherDoctor = doctorRepository.existsByPhoneAndIdNot(phone, currentDoctorId);

        boolean usedByOtherPatient = patientRepository.findByPhone(phone)
                .map(p -> !p.getUser().getId().equals(currentUserId))
                .orElse(false);

        return usedByOtherDoctor || usedByOtherPatient;
    }

}
