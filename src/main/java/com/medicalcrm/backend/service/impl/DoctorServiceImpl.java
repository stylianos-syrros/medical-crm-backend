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
    private final  AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Override
    public DoctorResponse createDoctor(CreateDoctorRequest request){

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Doctor doctor = DoctorMapper.toEntity(request, user);

        Doctor saved = doctorRepository.save(doctor);

        log.info("Doctor profile created for user {}", user.getId());

        return DoctorMapper.toResponse(saved);
    }

    @Override
    public DoctorResponse getProfile(Long doctorId){

        Doctor doctor = getDoctorEntity(doctorId);

        return DoctorMapper.toResponse(doctor);
    }

    @Override
    public DoctorResponse updateProfile(Long doctorId,
                                        UpdateDoctorRequest request) {

        Doctor doctor = getDoctorEntity(doctorId);

        DoctorMapper.updateEntity(doctor, request);

        log.info("Doctor {} updated profile", doctorId);

        return DoctorMapper.toResponse(doctor);
    }

    // PATIENT && APPOINTMENT
    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getMyPatients(Long doctorId){
        return appointmentRepository.findDistinctPatientsByDoctorId(doctorId)
                .stream()
                .map(PatientMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(Long doctorId){
        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointmentHistory(Long doctorId){
        return appointmentRepository
                .findByDoctorIdAndStatus(doctorId, AppointmentStatus.COMPLETED)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyUpcomingAppointments(Long doctorId) {

        return appointmentRepository
                .findByDoctorIdAndStatus(doctorId, AppointmentStatus.SCHEDULED)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    public void addNotes(Long doctorId,
                         Long appointmentId,
                         String notes){

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(()->
                        new NotFoundException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctorId)){
            throw new BusinessException("Not your appointment");
        }

        appointment.setNotes(notes);

        log.info("Doctor {} added notes to appointment {}",
                doctorId, appointmentId);
    }

    private Doctor getDoctorEntity(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));
    }
}