package com.medicalcrm.backend.service;

import com.medicalcrm.backend.model.*;

import java.util.List;

public interface PatientService {

    Patient getProfile(Long patientId);

    Patient updateProfile(Long patientId,
                          String firstName,
                          String lastName,
                          String phone);

    List<Doctor> getMyDoctors(Long patientId);

    List<Appointment> getMyAppointments(Long patientId);

    List<Appointment> getMyAppointmentHistory(Long patientId);

    List<Appointment> getMyUpcomingAppointments(Long patientId);

    void cancelAppointment(Long patientId,
                           Long appointmentId);
}
