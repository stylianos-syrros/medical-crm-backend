package com.medicalcrm.backend.service.impl;

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

    @Override
    public Patient getProfile(Long patientId){
        return patientRepository.findById(patientId)
                .orElseThrow(()->
                        new NotFoundException("Patient not found"));
    }

    @Override
    public Patient updateProfile(Long patientId,
                          String firstName,
                          String lastName,
                          String phone){
        Patient patient = getProfile(patientId);

        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setPhone(phone);

        log.info("Patient {} updated profile", patientId);

        return patient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> getMyDoctors(Long patientId){
        return appointmentRepository.
                findDistinctDoctorsByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getMyAppointments(Long patientId){
        return appointmentRepository.findByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getMyAppointmentHistory(Long patientId){
        return appointmentRepository.findByPatientIdAndStatus(patientId, AppointmentStatus.COMPLETED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getMyUpcomingAppointments(Long patientId){
        return appointmentRepository.findByPatientIdAndStatus(patientId, AppointmentStatus.SCHEDULED);
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
}
