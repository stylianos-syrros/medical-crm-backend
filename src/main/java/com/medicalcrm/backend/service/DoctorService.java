package com.medicalcrm.backend.service;

import com.medicalcrm.backend.model.*;

import java.util.List;

public interface DoctorService {

    Doctor getProfile(Long doctorId);

    Doctor updateProfile(Long doctorId,
                         String firstName,
                         String lastName,
                         String specialty,
                         String phone);


    List<Patient> getMyPatients(Long doctorId);

    List<Appointment> getMyAppointments(Long doctorId);

    List<Appointment> getMyAppointmentHistory(Long doctorId);

    List<Appointment> getMyUpcomingAppointments(Long doctorId);

    void addNotes(Long doctorId,
                  Long appointmentId,
                  String notes);

}
