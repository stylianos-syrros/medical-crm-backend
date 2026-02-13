package com.medicalcrm.backend.service.impl;

import com.medicalcrm.backend.exception.*;
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

    @Override
    public Doctor getProfile(Long doctorId){
        return doctorRepository.findById(doctorId)
                .orElseThrow(()->
                        new NotFoundException("Doctor not found"));
    }

    @Override
    public Doctor updateProfile(Long doctorId,
                         String firstName,
                         String lastName,
                         String specialty,
                         String phone){

        Doctor doctor = getProfile(doctorId);

        doctor.setFirstName(firstName);
        doctor.setLastName(lastName);
        doctor.setSpecialty(specialty);
        doctor.setPhone(phone);

        log.info("Doctor {} updated profile", doctorId);

        return doctor;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> getMyPatients(Long doctorId){
        return appointmentRepository.findDistinctPatientsByDoctorId(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getMyAppointments(Long doctorId){
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getMyAppointmentHistory(Long doctorId){
        return appointmentRepository.findByDoctorIdAndStatus(doctorId, AppointmentStatus.COMPLETED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getMyUpcomingAppointments(Long doctorId){
        return appointmentRepository.findByDoctorIdAndStatus(doctorId, AppointmentStatus.SCHEDULED);
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
}